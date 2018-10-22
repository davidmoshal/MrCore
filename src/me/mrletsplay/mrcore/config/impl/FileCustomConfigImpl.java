package me.mrletsplay.mrcore.config.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrletsplay.mrcore.config.impl.DefaultConfigParser.ConfigSectionDescriptor;
import me.mrletsplay.mrcore.config.impl.DefaultConfigParser.Marker;
import me.mrletsplay.mrcore.config.v2.ConfigException;
import me.mrletsplay.mrcore.config.v2.ConfigSection;
import me.mrletsplay.mrcore.config.v2.DefaultConfigMappers;
import me.mrletsplay.mrcore.config.v2.FileCustomConfig;
import me.mrletsplay.mrcore.config.v2.IncompatibleConfigVersionException;
import me.mrletsplay.mrcore.config.v2.ObjectMapper;

public class FileCustomConfigImpl implements FileCustomConfig {

	private static final String VERSION = "2.0";
	
	private File configFile;
	private ConfigSection mainSection;
	private Map<ObjectMapper<?, ?>, Integer> mappers;
	
	public FileCustomConfigImpl(File configFile) {
		this.configFile = configFile;
		this.mainSection = new DefaultConfigSectionImpl(this);
		this.mappers = new HashMap<>();
		registerMapper(DefaultConfigMappers.JSON_MAPPER);
	}
	
	@Override
	public ConfigSection getMainSection() {
		return mainSection;
	}

	@Override
	public void load(InputStream in) throws ConfigException {
		try(BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			List<String> lines = new ArrayList<>();
			String s;
			while((s = r.readLine()) != null) {
				lines.add(s + "\n");
			}
			r.close();
			DefaultConfigParser p = new DefaultConfigParser(lines.toArray(new String[lines.size()]));
			if(!p.hasMore()) {
				return;
			}
			String version = p.readVersionDescriptor();
			if(!version.equals(VERSION)) throw new IncompatibleConfigVersionException(version, VERSION);
			String header = p.readHeader();
			ConfigSectionDescriptor d = p.readSubsection(new Marker(0, 0), 0);
			mainSection.loadFromMap(d.getProperties());
			d.getComments().forEach(mainSection::setComment);
			if(header != null) mainSection.setComment(null, header);
		}catch(IOException e) {
			throw new ConfigException("Unexpected IO exception", e);
		}
	}

	@Override
	public void save(OutputStream out) {
		try(BufferedWriter o = new BufferedWriter(new OutputStreamWriter(out))) {
			DefaultConfigFormatter f = new DefaultConfigFormatter(o);
			f.writeConfigVersionDescriptor(VERSION);
			if(getHeader() != null) f.writeHeader(getHeader());
			f.writeSubsection(0, getMainSection().toMap(), getMainSection().commentsToMap());
			o.flush();
		}catch(IOException e) {
			throw new ConfigException("Unexpected IO exception", e);
		}
	}

	@Override
	public File getConfigFile() {
		return configFile;
	}

	@Override
	public void registerMapper(int priority, ObjectMapper<?, ?> mapper) {
		mappers.put(mapper, priority);
	}

	@Override
	public Map<ObjectMapper<?, ?>, Integer> getMappers() {
		return mappers;
	}
	
	
	
}
