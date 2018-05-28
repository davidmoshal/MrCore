package me.mrletsplay.mrcore.bukkitimpl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.mrletsplay.mrcore.config.ConfigExpansions.ExpandableCustomConfig;

public class BukkitCustomConfig extends ExpandableCustomConfig {
	
	public BukkitCustomConfig(File configFile, ConfigSaveProperty... defaultSaveProperties) {
		super(configFile, defaultSaveProperties);
		setFormatter(new BukkitConfigFormatter(this));
		registerMappers();
	}
	
	public BukkitCustomConfig(URL configURL, ConfigSaveProperty... defaultSaveProperties) {
		super(configURL, defaultSaveProperties);
		setFormatter(new BukkitConfigFormatter(this));
		registerMappers();
	}
	
	private void registerMappers() {
		registerMapper(new ObjectMapper<Location>(Location.class) {

			@Override
			public Map<String, Object> mapObject(Location loc) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("world", loc.getWorld().getName());
				map.put("x", loc.getX());
				map.put("y", loc.getY());
				map.put("z", loc.getZ());
				map.put("pitch", loc.getPitch());
				map.put("yaw", loc.getYaw());
				return map;
			}

			@Override
			public Location constructObject(Map<String, Object> map) {
				if(!requireKeys(map, "world", "x", "y", "z")) return null;
				return new Location(
							Bukkit.getWorld((String) map.get("world")),
							castGeneric(map.get("x"), Double.class),
							castGeneric(map.get("y"), Double.class),
							castGeneric(map.get("z"), Double.class),
							castGeneric(map.get("yaw"), Float.class),
							castGeneric(map.get("pitch"), Float.class)
						);
			}
			
		});
		
		registerMapper(new ObjectMapper<ItemStack>(ItemStack.class) {

			@Override
			public Map<String, Object> mapObject(ItemStack it) {
				Map<String, Object> map = new HashMap<>();
				map.put("type", it.getType());
				map.put("amount", it.getAmount());
				map.put("durability", it.getDurability());
				if(it.hasItemMeta()) {
					ItemMeta m = it.getItemMeta();
					map.put("name", m.getDisplayName());
					map.put("lore", m.getLore());
					if(!m.getEnchants().isEmpty()) {
						HashMap<String, Integer> enchMap = new HashMap<>();
						for(Map.Entry<Enchantment, Integer> ench : m.getEnchants().entrySet()) {
							enchMap.put(ench.getKey().getName(), ench.getValue());
						}
						map.put("enchantments", enchMap);
					}
					if(!m.getItemFlags().isEmpty()) {
						map.put("flags", m.getItemFlags().stream().map(f -> f.name()).collect(Collectors.toList()));
					}
				}
				return map;
			}

			@SuppressWarnings("unchecked")
			@Override
			public ItemStack constructObject(Map<String, Object> map) {
				if(!map.containsKey("type")) return null;
				ItemStack it = new ItemStack(Material.getMaterial((String) map.get("type")),
						castGeneric(map.getOrDefault("amount", 1), Integer.class),
						castGeneric(map.getOrDefault("durability", 0), Short.class));
				ItemMeta m = it.getItemMeta();
				m.setDisplayName((String) map.get("name"));
				m.setLore((List<String>) map.getOrDefault("lore", new ArrayList<>()));
				Map<String, Integer> enchs = castGenericMap((Map<String, ?>) map.getOrDefault("enchantments", new HashMap<>()), Integer.class);
				enchs.forEach((en, lvl) -> m.addEnchant(Enchantment.getByName(en), lvl, true));
				List<String> flags = (List<String>) map.getOrDefault("flags", new ArrayList<>());
				flags.forEach(f -> m.addItemFlags(ItemFlag.valueOf(f)));
				return it;
			}
			
		});
	}
	
	public static class BukkitConfigFormatter extends ExpandableConfigFormatter {

		private BukkitCustomConfig config;
		
		public BukkitConfigFormatter(BukkitCustomConfig config) {
			super(config);
			this.config = config;
		}
		
		@Override
		public FormattedProperty formatObject(Object o) {
			FormattedProperty fp = super.formatObject(o);
			if(fp.isSpecific()) return fp;
			
//			if(o instanceof ConfigurationSerializable) {
//				return FormattedProperty.map(((ConfigurationSerializable) o).serialize());
//			}
			
			ObjectMapper<?> mapper = config.getMappers().stream().filter(m -> m.canMap(o)).findFirst().orElse(null);
			
			if(mapper == null) return fp;
			
			return FormattedProperty.map(mapper.map(o));
		}
		
		@Override
		public BukkitCustomConfig getConfig() {
			return config;
		}
		
	}
	
	public Location getLocation(String key) {
		return getMappable(key, Location.class);
	}
	
	public Location getLocation(String key, Location defaultVal, boolean applyDefault) {
		Location o = getMappable(key, Location.class);
		if(o != null) return o;
		if(applyDefault) set(key, defaultVal);
		return defaultVal;
	}
	
	public List<Location> getLocationList(String key) {
		return getMappableList(key, Location.class);
	}
	
	public List<Location> getLocationList(String key, List<Location> defaultVal, boolean applyDefault) {
		List<Location> o = getMappableList(key, Location.class);
		if(o != null) return o;
		if(applyDefault) set(key, defaultVal);
		return defaultVal;
	}
	
	public ItemStack getItemStack(String key) {
		return getMappable(key, ItemStack.class);
	}
	
	public ItemStack getItemStack(String key, ItemStack defaultVal, boolean applyDefault) {
		ItemStack o = getMappable(key, ItemStack.class);
		if(o != null) return o;
		if(applyDefault) set(key, defaultVal);
		return defaultVal;
	}
	
	public List<ItemStack> getItemStackList(String key) {
		return getMappableList(key, ItemStack.class);
	}
	
	public List<ItemStack> getItemStackList(String key, List<ItemStack> defaultVal, boolean applyDefault) {
		List<ItemStack> o = getMappableList(key, ItemStack.class);
		if(o != null) return o;
		if(applyDefault) set(key, defaultVal);
		return defaultVal;
	}
	
//	public static void setTexture(SkullMeta im, String url) {
//		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
//		profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", new String(Base64.getEncoder().encode(("{textures:{SKIN:{url:\""+url+"\"}}}").getBytes()))));
//		Field profileField = null;
//		try {
//			profileField = im.getClass().getDeclaredField("profile");
//			profileField.setAccessible(true);
//			profileField.set(im, profile);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//	}
//	
//	public static void setRawTexture(SkullMeta im, String raw) {
//		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
//		profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", raw));
//		Field profileField = null;
//		try {
//			profileField = im.getClass().getDeclaredField("profile");
//			profileField.setAccessible(true);
//			profileField.set(im, profile);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//	}
//	
//	public static String getRawTexture(SkullMeta m) {
//		try {
//			Field profileField = m.getClass().getDeclaredField("profile");
//			profileField.setAccessible(true);
//			GameProfile profile = (GameProfile) profileField.get(m);
//			if(profile != null) {
//				Collection<com.mojang.authlib.properties.Property> txt = profile.getProperties().get("textures");
//				Iterator<com.mojang.authlib.properties.Property> it = txt.iterator();
//				if(it.hasNext()) {
//					return it.next().getValue();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
}
