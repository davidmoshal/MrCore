package me.mrletsplay.mrcore.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class IOUtils {
	
	public static void transfer(InputStream from, OutputStream to) throws IOException {
		transfer(from, to, 4096);
	}
	
	public static void transferUntilUnavailable(InputStream from, OutputStream to) throws IOException {
		transferUntilUnavailable(from, to, 4096);
	}
	
	public static void transfer(InputStream from, OutputStream to, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int len;
		while((len = from.read(buf)) > 0) {
			to.write(buf, 0, len);
		}
	}
	
	public static void transferUntilUnavailable(InputStream from, OutputStream to, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int len;
		while((len = from.read(buf)) > 0) {
			to.write(buf, 0, len);
			if(from.available() == 0) break;
		}
	}
	
	public static void transferSafely(InputStream from, OutputStream to) throws FriendlyException {
		try {
			transfer(from, to);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static void transferSafely(InputStream from, OutputStream to, int bufferSize) throws FriendlyException {
		try {
			transfer(from, to, bufferSize);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static byte[] readAllBytes(InputStream from) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		transfer(from, bOut);
		return bOut.toByteArray();
	}
	
	public static byte[] readAllBytesSafely(InputStream from) throws FriendlyException {
		try {
			return readAllBytes(from);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static byte[] readBytesUntilUnavailable(InputStream from) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		transferUntilUnavailable(from, bOut);
		return bOut.toByteArray();
	}
	
	public static byte[] readBytesUntilUnavailableSafely(InputStream from) {
		try {
			return readBytesUntilUnavailable(from);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static boolean createFile(File file) throws FriendlyException {
		if(file.exists()) return true;
		file = file.getAbsoluteFile();
		if(file.getParentFile() != null) file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
		return false;
	}
	
	public static void deleteFile(File file) throws FriendlyException {
		if(!file.exists()) return;
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				deleteFile(f);
			}
			file.delete();
		}else {
			file.delete();
		}
	}
	
	public static void writeBytes(File f, byte[] bytes) throws FriendlyException {
		IOUtils.createFile(f);
		try(FileOutputStream fOut = new FileOutputStream(f)) {
			fOut.write(bytes);
		}catch(IOException e) {
			throw new FriendlyException(e);
		}
	}
	
}
