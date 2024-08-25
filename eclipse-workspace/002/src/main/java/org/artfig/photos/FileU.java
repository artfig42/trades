package org.artfig.photos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileU {

	private static Gson gson = new GsonBuilder().create();

	public static final void m(Object message) {
		System.out.println(message);
	}

	/**
	 * Fetch the entire contents of a text file, and return it in a String. This
	 * style of implementation does not throw Exceptions to the caller.
	 *
	 * @param aFile is a file which already exists and can be read.
	 */
	static public String getFileContents(File aFile) {
		StringBuilder contents = new StringBuilder();
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line MINUS the
				 * newline. it returns null only for the END of the stream. it returns an empty
				 * String if two newlines appear in a row.
				 */
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return contents.toString();
	}

	static public final String removeExtraWhitespace(String theString) {
		return theString.replaceAll("\\s+", " ");
	}

	static public final String fixHtmlText(String theString) {
		return theString.replaceAll(">[ ]+", ">").replaceAll("[ ]+<", "<");
	}

	static public final String fixJsonText(String theString) {
		return theString.replaceAll("'[ ]+", "'").replaceAll("[ ]+'", "'");
	}

	static public final String parsePrep(String theString) {
		return fixJsonText(fixHtmlText(removeExtraWhitespace(theString)));
	}

	static public String getContentsForSplit(File aFile) {
		StringBuilder contents = new StringBuilder();
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line MINUS the
				 * newline. it returns null only for the END of the stream. it returns an empty
				 * String if two newlines appear in a row.
				 */
				while ((line = input.readLine()) != null) {
					contents.append(parsePrep(line));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

	static public List<String> getLinesWhichMatch(File aFile, String match, String split_on) {
		List<String> containsList = new ArrayList<String>();
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line MINUS the
				 * newline. it returns null only for the END of the stream. it returns an empty
				 * String if two newlines appear in a row.
				 */
				while ((line = input.readLine()) != null) {
					// FileU.v(line);
					if (Pattern.matches(match, line)) {
						line = line.substring(line.indexOf(split_on));
						for (String token : line.split(split_on)) {
							token = split_on + token;
							if (Pattern.matches(match, token)) {
								containsList.add(token);
							}
						}
					}
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return containsList;
	}

	static public String getContents(String filename) {
		return FileU.getFileContents(new File(filename));
	}

	public static boolean fileExists(String filepath) {
		File theFile = new File(filepath);
		return theFile.exists();
	}

	public static void doSubmit(String url, Map<String, String> data, String filename) throws Exception {
		doSubmit(url, data, new File(filename));
	}

	public static void doSubmit(String url, Map<String, String> data, File to_file) throws Exception {
		URL siteUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream out = new DataOutputStream(conn.getOutputStream());

		String content = "";
		int i = 0;
		for (String key : data.keySet()) {
			if (i++ != 0) {
				content += "&";
			}
			content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
		}
		System.out.println("content=" + content);
		out.writeBytes(content);
		out.flush();
		out.close();

		InputStream reader = conn.getInputStream();

		to_file.getParentFile().mkdirs();
		FileOutputStream writer = new FileOutputStream(to_file);
		byte[] buffer = new byte[153600];
		int bytesRead = 0;

		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
		}

		writer.close();
		reader.close();

	}

	public static String downloadToString(String fromUrl) throws Exception {
		StringBuilder sb = new StringBuilder();
		String line = null;

		BufferedReader reader = null;
		try {

			URL url = new URL(fromUrl);

			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
			urlconn.setConnectTimeout(5000);
			urlconn.setReadTimeout(0);
			urlconn.connect();
			urlconn.getResponseMessage();
			urlconn.getContentType();

			reader = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return sb.toString();
	} // downloadToString

	public static String downloadForParsing(String fromUrl) {
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {

			URL url = new URL(fromUrl);

			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
			urlconn.setConnectTimeout(5000);
			urlconn.setReadTimeout(0);
			urlconn.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				sb.append(parsePrep(line));
			}

			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Download exception:" + e.getMessage() + " url=" + fromUrl);
		}
		return sb.toString();
	}

	public static final String asJSON(Object to_write) {
		try {
			return gson.toJson(to_write);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	public static void writeToFile(Object to_write, File to_file) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(to_file));
			out.write(gson.toJson(to_write));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static <T extends Object> T readFromFile(File from_file, Class<T> classOfT) {
		return gson.fromJson(FileU.readFromFile(from_file), classOfT);
	}

	public static String readFromFile(File from_file) {
		StringBuilder results = new StringBuilder();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(from_file));
			while ((line = reader.readLine()) != null) {
				results.append(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results.toString();
	} // readFromFile

	public static void logToFile(String to_write, String filename) {
		logToFile(to_write, new File(filename));
	}

	public static void logToFile(String to_write, File logFile) {
		String fname = null;

		try {
			fname = logFile.getCanonicalPath();
		} catch (IOException e1) {
		}

		for (int attempt = 0; attempt < 5; attempt++) {
			try {
				// if file doesn't exists, then create it
				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				// true = append file
				FileWriter fileWriter = new FileWriter(logFile, true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
				bufferWritter.write(to_write + "\r\n");
				bufferWritter.close();

			} catch (IOException e) {
				System.out.println("File:" + fname);
				continue;
			}
			return;
		}
		throw new RuntimeException("File:" + fname);
	}

	public static String getFilenameFromUrl(String theUrl) {
		int slashIndex = theUrl.lastIndexOf('/');
		int dotIndex = theUrl.lastIndexOf('.', slashIndex);
		// System.out.println(theUrl + ":" + slashIndex + ":" + dotIndex);
		String filename;
		if (dotIndex < slashIndex) {
			filename = theUrl.substring(slashIndex + 1);
		} else {
			filename = theUrl.substring(slashIndex + 1, dotIndex);
		}
		return filename;
	}

	public static String fileName(File theFile) {
		String path = theFile.getAbsolutePath();
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}

	public static File childFile(File folder, String filename) {
		return new File(folder.getAbsolutePath() + File.separator + filename);
	}

	public static void copyFileToDir(File sourceFile, File destFolder) {

		if (sourceFile == null || sourceFile.exists() == false) {
			return;
		}

		try {

			if (!destFolder.exists()) {
				destFolder.mkdirs();
			}

			String filename = fileName(sourceFile);
			File destFile = childFile(destFolder, filename);

			if (destFile.exists()) {
				return;
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void moveOrDelete(File sourceFile, File destFile, boolean checkSize, int delay) {

		if (sourceFile == null || sourceFile.exists() == false) {
			return;
		}

		try {
			if (destFile.exists()) {
				if ((checkSize ? sourceFile.length() == destFile.length() : true)) {
					FileUtils.forceDelete(sourceFile);
					System.out.println("deleted " + sourceFile);
				}
			} else {
				destFile.getParentFile().mkdirs();
				FileUtils.moveFile(sourceFile, destFile);
				System.out.println("created " + destFile);
			}
			Thread.sleep(delay);
		} catch (IOException ex) {
			Logger.getLogger(FileU.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(FileU.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void copyFile(File sourceFile, File destFile) {

		if (sourceFile == null || sourceFile.exists() == false) {
			return;
		}

		try {
			if (!destFile.exists()) {
				destFile.getParentFile().mkdirs();
				FileUtils.copyFile(sourceFile, destFile);
				System.out.println("created " + destFile);
			}
		} catch (IOException ex) {
			Logger.getLogger(FileU.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
