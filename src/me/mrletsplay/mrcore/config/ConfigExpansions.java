package me.mrletsplay.mrcore.config;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigExpansions {
	
	public static class ExpandableCustomConfig extends CustomConfig {
		
		private List<ObjectMapper<?>> mappers = new ArrayList<>();
		
		public ExpandableCustomConfig(File configFile, ConfigSaveProperty... defaultSaveProperties) {
			super(configFile, defaultSaveProperties);
			setFormatter(new ExpandableConfigFormatter(this));
		}

		public ExpandableCustomConfig(URL configURL, ConfigSaveProperty... defaultSaveProperties) {
			super(configURL, defaultSaveProperties);
			setFormatter(new ExpandableConfigFormatter(this));
		}
		
		public void registerMapper(ObjectMapper<?> mapper) {
			mappers.add(mapper);
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getMappable(String key, Class<T> mappingClass) {
			ObjectMapper<T> mapper = (ObjectMapper<T>) mappers.stream().filter(m -> m.mappingClass.equals(mappingClass)).findFirst().orElse(null);
			try {
				return mapper.constructObject(getMap(key));
			} catch(Exception e) {
				throw new InvalidTypeException(key, "Failed to parse into "+mappingClass.getName(), e);
			}
		}
		
		@SuppressWarnings("unchecked")
		public <T> List<T> getMappableList(String key, Class<T> mappingClass) {
			ObjectMapper<T> mapper = (ObjectMapper<T>) mappers.stream().filter(m -> m.mappingClass.equals(mappingClass)).findFirst().orElse(null);
			List<Map<String, Object>> list = getMapList(key);
			try {
				return list.stream().map(e -> mapper.constructObject(e)).collect(Collectors.toList());
			} catch(Exception e) {
				throw new InvalidTypeException(key, "Failed to parse into "+mappingClass.getName(), e);
			}
		}
		
		public List<ObjectMapper<?>> getMappers() {
			return mappers;
		}
		
		public static abstract class ObjectMapper<T> {
			
			public Class<T> mappingClass;
			
			public ObjectMapper(Class<T> clazz) {
				this.mappingClass = clazz;
			}
			
			public boolean canMap(Object o) {
				if(o == null) return false;
				return mappingClass.isAssignableFrom(o.getClass());
			}
			
			public Map<String, Object> map(Object o) {
				return mapObject(mappingClass.cast(o));
			}
			
			public abstract Map<String, Object> mapObject(T object);
			
			public abstract T constructObject(Map<String, Object> map);
			
		}
		
		public static class ExpandableConfigFormatter extends DefaultConfigFormatter {

			private ExpandableCustomConfig config;
			
			public ExpandableConfigFormatter(ExpandableCustomConfig config) {
				super(config);
				this.config = config;
			}
			
			@Override
			public FormattedProperty formatObject(Object o) {
				FormattedProperty fp = super.formatObject(o);
				if(fp.isSpecific()) return fp;
				
				ObjectMapper<?> mapper = config.mappers.stream().filter(m -> m.canMap(o)).findFirst().orElse(null);
				if(mapper != null) {
					return FormattedProperty.map(mapper.map(o));
				}
				
				return fp;
			}
			
		}
		
	}
	
}
