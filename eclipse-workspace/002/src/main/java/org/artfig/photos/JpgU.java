/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.artfig.photos;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * https://github.com/drewnoakes/metadata-extractor/wiki/GettingStarted
 *
 * @author Arthur
 */
public class JpgU {

	// private static final Path basepath = Paths.get("o:/Staging/Pictures");
//	private static final Path findpath = Paths.get("o:/intake");
//	private static final Path lookpath = Paths.get("J:/intake_clean");
	private static Map<String, CatalogFile> catalogFolders = new TreeMap<>();
	private static Map<String, CatalogFile> identityTags = new TreeMap<>();
	private static final String KNOWN_SOURCES = "DMC\\-FS15|NIKON D80|KODAK DX6490 ZOOM DIGITAL CAMERA|NIKON D40X|LG\\-D800|NIKON D7200|NIKON D850";
	private static Set<String> sources = new TreeSet<>();

	public static void cleanCatalogFile(Path basepath, CatalogFile old) {
		File old_file = old.filepath.toFile();
		if (catalogFolders.containsKey(old.canonical)) {
			CatalogFile cat = catalogFolders.get(old.canonical);
			if (old.filesize == cat.filesize) {
				old_file.deleteOnExit();
				System.out.println("delete: " + old);
			} else {
				old_file.delete();
				old.named = "Check";
				File new_file = old.getFileAt(basepath).toFile();
				if (!new_file.exists()) {
					System.out.println("not: " + old);
					System.out.println("     " + old.getFileAt(basepath));
					try {
						// FileU.moveOrDelete(old_file, new_file, true);
					} catch (Exception ex) {
						Logger.getLogger(JpgU.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		} else {
			old.named = null;
			File new_file = old.getFileAt(basepath).toFile();
			if (!new_file.exists()) {
				System.out.println("not: " + old);
				System.out.println("     " + old.getFileAt(basepath));
				FileU.moveOrDelete(old_file, new_file, true, 500);
			}
		}
//        if (named == null && namedFolders.containsKey(fname)) {
//            System.out.println(fname + "::" + namedFolders.get(fname) + "::" + file);
		// File tobe_file = standardName(file, namedFolders.get(fname));
//        }

	} // cleanCatalogFile

	public static void cleanCatalogFolder(Path basepath, Path folder) {
		for (File file : folder.toFile().listFiles()) {
			if (file.isDirectory()) {
				cleanCatalogFolder(basepath, file.toPath());
			} else {
				CatalogFile cat = new CatalogFile(file);
				if (cat.extension.matches("mov|jpg")) {
					cleanCatalogFile(basepath, cat);
				} else if (cat.extension.matches("nef")) {
					cat.named = "NEF";
					File new_file = cat.getFileAt(basepath).toFile();
					FileU.moveOrDelete(file, new_file, true, 500);
				}
			}
		}
	} // cleanCatalogFolder

	public static void cleanFolder(Path basepath, Path folder) {
		// System.out.println("folder " + folder);
		for (File file : folder.toFile().listFiles()) {
			if (file.isDirectory()) {
				cleanFolder(basepath, file.toPath());
			} else {
				CatalogFile find = new CatalogFile(file);
				if (find.extension.matches("jpg|mov")) {
					if (catalogFolders.containsKey(find.canonical)) {
						if (find.filesize == catalogFolders.get(find.canonical).filesize) {
							try {
								System.out.println("delete " + find);
								file.delete();
								Thread.sleep(200);
							} catch (InterruptedException ex) {
								Logger.getLogger(JpgU.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					} else {
						find.named = null;
						if (find.source != null && find.source.matches(KNOWN_SOURCES)) {
							File new_file = find.getFileAt(basepath).toFile();
							File old_file = find.filepath.toFile();
							if (!new_file.exists()) {
								System.out.println("not: " + old_file);
								System.out.println("     " + new_file);
								FileU.moveOrDelete(old_file, new_file, true, 500);
							}
						}
					}
				}
			}
		}
	} // findFolder

	public static void findFolder(Path findpath, Path folder) {
		// System.out.println("folder " + folder);
		for (File file : folder.toFile().listFiles()) {
			if (file.isDirectory()) {
				findFolder(findpath, file.toPath());
			} else {
				CatalogFile old = new CatalogFile(file);
				if (catalogFolders.containsKey(old.canonical)) {
					if (!(old.named == null
							|| old.named.toLowerCase().matches("dcim|facebook|fb|.*pana|.+___|nikon.+"))) {
						File new_file = old.getFileAt(findpath).toFile();
						FileU.moveOrDelete(file, new_file, true, 500);
					}
				} else if (!old.extension.matches("nef|db|dsc")) {
					old.named = "Missing";
					File new_file = old.getFileAt(findpath).toFile();
					FileU.copyFile(file, new_file);
				}
			}

		}
	} // findFolder

	// File must be standard here
	public static String getGroupName(String parent) {
		String named = null;
		if (!parent.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2}$")) {
			// named = parent;
			if (parent.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2} (.+)")) {
				named = parent.substring(11);
//                if (!namedFolders.containsKey(fname)) {
				// System.out.println(fname + ":" + named);
				// namedFolders.put(fname, named);
				// }
			}
		}
//        if (named == null && namedFolders.containsKey(fname)) {
//            System.out.println(fname + "::" + namedFolders.get(fname) + "::" + file);
		// File tobe_file = standardName(file, namedFolders.get(fname));
//        }
		return named;
	} // getGroupName

	public static void main(String[] args) {
		// final Path basepath = Paths.get("o:/Staging/Pictures");
		final Path basepath = Paths.get("g:/photos");
		final Path findpath = Paths.get("o:/intake");
		final Path lookpath = Paths.get("J:/intake_clean");
		System.out.println("basepath: " + basepath);
		System.out.println("findpath: " + findpath);
		System.out.println("lookpath: " + lookpath);
		// This will establish files that should not move
	//	processTaggedFolder(basepath.resolve("tagged"), basepath.resolve("tagged"));

		// This will establish files that will be kept in sorted order
	//	processSortedFolder(basepath.resolve("sorted"), basepath.resolve("sorted"));

		// This will establish the new files that will be kept in sorted order
	//	processIntakeFolder(basepath.resolve("sorted_intake_" + System.currentTimeMillis()), basepath.resolve("n:/intake"));

		// removeEmptyFolders(Paths.get("e:/"));
		removeEmptyFolders(Paths.get("g:/photos/"));
		// processCatalogFolder(basepath, Paths.get("g:/photos/tagged"));
		// cleanCatalogFolder(findpath);
		// findFolder(lookpath);
		// cleanFolder(lookpath);
//		cleanFolder(findpath);
		System.out.println("namedFolders_size: " + catalogFolders.size());
		System.out.println("identityTags_size: " + identityTags.size());
		System.out.println("sources:           " + sources);
		System.out.println("freemem:           " + Runtime.getRuntime().freeMemory());
	}

	public static void processCatalogFile(final Path basepath, final CatalogFile cat) {
		final File new_file = cat.getFileAt(basepath).toFile();

		System.out.println("   " + cat.filepath + "\n" + new_file.toString());

		if (cat.filepath.startsWith(basepath)) {
			FileU.moveOrDelete(cat.filepath.toFile(), new_file, true, 100);
		} else if (cat.filepath.compareTo(new_file.toPath()) != 0) {
			FileU.copyFile(cat.filepath.toFile(), new_file);
		}

		// if (cat.named != null) {
		if (!catalogFolders.containsKey(cat.canonical)) {
			catalogFolders.put(cat.canonical, cat);
		} else {
			CatalogFile old = catalogFolders.get(cat.canonical);
			System.out.println("   dup: " + cat.filepath);
			System.out.println("       old: " + old.filepath);
		}
		// }
	} // processCatalogFile

	public static void processCatalogFolder(final Path basepath, final Path folder) {
		File[] files = folder.toFile().listFiles();
		if (files == null) {
			return;
		}
		if (files.length > 0) {
			for (final File file : files) {
				if (file.isDirectory()) {
					processCatalogFolder(basepath, file.toPath());
				} else {
					final CatalogFile cat = new CatalogFile(file);
					if (cat.extension.matches("mov|jpg")) {
						if (cat.source != null && !cat.source.matches(KNOWN_SOURCES)) {
							sources.add(cat.source);
							System.out.println(cat.filepath);
						}
						try {
							System.out.println(cat);

							// processCatalogFile(basepath, cat);
						} catch (Exception e) {

						}
					}
				}
			}
			files = folder.toFile().listFiles();
		}

		if (files == null) {
			return;
		}

		System.out.println(files.length + " " + folder);
		if (files.length == 0) {
			folder.toFile().delete();
		} else if (files.length == 1) {
			System.out.println(files[0]);
		}

	} // processCatalogFolder

	public static void processFolder(Path basepath, Path folder) {
		for (File file : folder.toFile().listFiles()) {
			if (file.isDirectory()) {
				processFolder(basepath, file.toPath());
			} else {
				renameToStandard(basepath, file);
			}
		}
	} // processFolder

	public static void processIntakeFolder(final Path baseSortedPath, final Path folder) {
		System.out.println("processIntakeFolder-->" + folder);

		File[] files = folder.toFile().listFiles();
		if (files == null || files.length < 1) {
			return;
		}

		for (final File file : files) {
			if (file.isDirectory()) {
				processIntakeFolder(baseSortedPath, file.toPath());
			} else {
				CatalogFile cat = new CatalogFile(file);
				if (cat.extension.matches("mov|jpg")) {
					try {
						if (!identityTags.containsKey(cat.identity_stamp)) {
							final File new_file = cat.getFileAt(baseSortedPath).toFile();

							System.out.println(cat.filepath + "\n to:" + new_file.toString());

							FileU.copyFile(cat.filepath.toFile(), new_file);

							cat = new CatalogFile(new_file);

							identityTags.put(cat.identity_stamp, cat);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	} // processIntakeFolder

	public static void processSortedFolder(final Path baseSortedPath, final Path folder) {
		File[] files = folder.toFile().listFiles();

		if (files == null || files.length < 1) {
			return;
		}

		for (final File file : files) {
			if (file.isDirectory()) {
				processSortedFolder(baseSortedPath, file.toPath());
			} else {
				final CatalogFile cat = new CatalogFile(file);
				if (cat.extension.matches("mov|jpg")) {
					try {
						if (identityTags.containsKey(cat.identity_stamp)) {
							System.out.println("dup: " + file);
							System.out.println("     " + identityTags.get(cat.identity_stamp).filepath);
							// file.deleteOnExit();
						} else {
							if (!cat.fname.startsWith(cat.identity_stamp)) {
								System.out.println("SORTED_WRONG:" + file + "\n" + cat.identity_stamp);
							}
							identityTags.put(cat.identity_stamp, cat);
							if (identityTags.size() % 100 == 0) {
								System.out.println("processSortedFolder[" + identityTags.size() + "]-->" + folder);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	} // processSortedFolder

	public static void processTaggedFolder(final Path baseTaggedPath, final Path folder) {
		File[] files = folder.toFile().listFiles();

		if (files == null || files.length < 1) {
			return;
		}
		for (final File file : files) {
			if (file.isDirectory()) {
				processTaggedFolder(baseTaggedPath, file.toPath());
			} else {
				final CatalogFile cat = new CatalogFile(file);
				if (cat.extension.matches("mov|jpg")) {
					try {
						if (identityTags.containsKey(cat.identity_stamp)) {
							System.out.println("dup: " + file);
							System.out.println("     " + identityTags.get(cat.identity_stamp).filepath);
							// file.deleteOnExit();
						} else {
							identityTags.put(cat.identity_stamp, cat);
							if (!cat.fname.startsWith(cat.identity_stamp)) {
								System.out.println("WRONG:" + file);
							}
							if (identityTags.size() % 444 == 0) {
								System.out.println("processTaggedFolder[" + identityTags.size() + "]-->" + folder);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	} // processTaggedFolder

	public static void removeEmptyFolders(final Path folder) {
		File[] files = folder.toFile().listFiles();
		if (files == null) {
			return;
		}
		if (files.length > 0) {
			for (final File file : files) {
				if (file.isDirectory()) {
					removeEmptyFolders(file.toPath());
				}
			}
			files = folder.toFile().listFiles();
			if (files == null) {
				return;
			}
		}

		if (files.length == 0) {
			folder.toFile().delete();
			System.out.println("Empty folder: " + folder);
		}

	} // removeEmptyFolders

	public static void renameToStandard(Path basepath, File file) {
		String filename = file.getName();

		if (!(filename.toLowerCase().endsWith(".mov") || filename.toLowerCase().endsWith(".jpg")
				|| filename.toLowerCase().endsWith(".nef"))) {
			return;
		}

		CatalogFile cat = new CatalogFile(file);
//        String parent = file.getParentFile().getName();
		String prefix = "";
		String named = getGroupName(file.toString());
		String new_filename = prefix + " ^ " + filename;

		Path new_dirpath = basepath.resolve(prefix.substring(0, 4)).resolve(prefix.substring(0, 10));
		Path new_filepath = new_dirpath.resolve(new_filename);
		if (new_filepath.equals(file.toPath())) {
			return;
		}
		if (new_filepath.toFile().exists()) {
			long new_filesize = new_filepath.toFile().length();
			long old_filesize = file.length();
			if (old_filesize == new_filesize) {
				// System.out.println("new_filepath: " + new_filepath + " size=" +
				// new_filesize);
				// System.out.println("old_filepath: " + file + " size=" + old_filesize);
				// file.deleteOnExit();
			}
		} else {
			System.out.println("new_filepath: " + new_filepath);
			System.out.println("   old_filepath: " + file);
			// try {
			// FileUtils.moveFileToDirectory(file, new_dirpath.toFile(), true);
			// } catch (IOException ex) {
			// Logger.getLogger(JpgU.class.getName()).log(Level.SEVERE, null, ex);
			// }
		}
		File renameFile = Paths.get(file.getParent()).resolve(new_filename).toFile();
		if (!new_filepath.toFile().exists()) {
			System.out.println("Missing new_filepath: " + new_filepath);
			// file.renameTo(new_file);
			// FileUtils.moveFileToDirectory(file, new_filepath, true);
		} else {
			// System.out.println("Exists file: " + ren_filepath);
		}
	} // renameToStandard

	public static void renameWithSizeCheck(File file, File renameFile) {
		if (renameFile.exists()) {
			if (file.length() == renameFile.length()) {
				System.out.println("Delete " + file + " [size=" + renameFile.length() + "]");
				// file.deleteOnExit();
			}
		} else {
			try {
				// FileUtils.moveFileToDirectory(file, renameFile.getParentFile(), true);
				System.out.println("Moved " + file + " --> " + renameFile);
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				Logger.getLogger(JpgU.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
} // JpgU
