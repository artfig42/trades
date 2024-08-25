/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.artfig.photos;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Arthur
 */
public final class CatalogFile {

	private static final Gson gson = new GsonBuilder().create();
	private static final String strip_time = "^([\\d]{4}-[\\d]{2}-[\\d]{2} [\\d]{2}.[\\d]{2}.[\\d]{2}) [\\d]{10} [\\^] (.+)";
	private static final Pattern strip_time_pattern = Pattern.compile(strip_time);
	private static final String source_name = "^(.+) [\\^] (.+)$";
	private static final Pattern source_name_pattern = Pattern.compile(source_name);
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

	public String canonical;
	public String identity_stamp;
	public String source;
	public String fname;
	public String day_time;
	public String yrmonday;
	public String year;
	public String image_name;
	public String named;
	public String parent;
	public Date date_taken;
	public long filesize;
	public transient Path filepath;
	public String extension;

	@Override
	public String toString() {
		return gson.toJson(this);
	}

	public CatalogFile(File file) {
		filepath = file.toPath();
		filesize = file.length();
		source = null;
		fname = file.getName();
		extension = FilenameUtils.getExtension(file.toString()).toLowerCase();
		Matcher m = strip_time_pattern.matcher(fname);
//		if (m.find()) {
	//		canonical = m.group(0);
	//		day_time = m.group(1);
	//		image_name = m.group(2);
	//		m = source_name_pattern.matcher(image_name);
	//		if (m.find()) {
	//			source = m.group(1);
	//			image_name = m.group(2);
	////		}
	//	} else {
			m = source_name_pattern.matcher(fname);
			if (m.find()) {
				image_name = m.group(2);
			} else {
				image_name = fname;
			}
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);

				ExifSubIFDDirectory exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
				ExifIFD0Directory exifIFD0Directory = metadata.getDirectory(ExifIFD0Directory.class);

				date_taken = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				source = exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL).replace("/", "");

				day_time = format.format(date_taken);
				identity_stamp = day_time + " " + String.format("%010d", filesize) + " ^ " + source;
			} catch (Exception ex) {
				date_taken = new Date(file.lastModified());
				day_time = format.format(new Date(file.lastModified()));
				identity_stamp = day_time + " " + String.format("%010d", filesize);
			}
			canonical = identity_stamp + " ^ " + image_name;
	//	}

		yrmonday = day_time.substring(0, 10);
		year = yrmonday.substring(0, 4);
		parent = file.getParentFile().getName();
		named = null;
		if (!parent.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2}$")) {
			if (parent.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2} (.+)")) {
				named = parent.substring(11);
			} else {
				named = parent;
			}
		}
	}

	public Path getFileAt(Path basepath) {
		return basepath.resolve(year).resolve(yrmonday + (source == null ? "" : (" " + source))).resolve(canonical);
	}
} // CatalogFile
