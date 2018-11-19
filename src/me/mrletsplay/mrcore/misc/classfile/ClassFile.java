package me.mrletsplay.mrcore.misc.classfile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.misc.EnumFlagCompound;
import me.mrletsplay.mrcore.misc.classfile.attribute.Attribute;
import me.mrletsplay.mrcore.misc.classfile.attribute.AttributeCode;
import me.mrletsplay.mrcore.misc.classfile.attribute.AttributeRaw;
import me.mrletsplay.mrcore.misc.classfile.attribute.DefaultAttributeType;
import me.mrletsplay.mrcore.misc.classfile.pool.ConstantPool;
import me.mrletsplay.mrcore.misc.classfile.pool.ConstantPoolTag;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolClassEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolDoubleEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolFieldRefEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolFloatEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolIntegerEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolInterfaceMethodRefEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolInvokeDynamicEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolLongEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolMethodHandleEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolMethodRefEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolMethodTypeEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolNameAndTypeEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolStringEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolUTF8Entry;

public class ClassFile {

	private int minorVersion, majorVersion;
	private ConstantPool constantPool;
	private EnumFlagCompound<ClassAccessFlag> accessFlags;
	private ConstantPoolClassEntry thisClass;
	private ConstantPoolClassEntry superClass;
	private ConstantPoolClassEntry[] interfaces;
	private ClassField[] fields;
	private ClassMethod[] methods;
	private Attribute[] attributes;
	
	public ClassFile(File fromFile) throws IOException {
		this(new FileInputStream(fromFile));
	}
	
	public ClassFile(InputStream fromInputStream) throws IOException {
		DataInputStream in = new DataInputStream(fromInputStream);
		if(in.readInt() != 0xCAFEBABE) throw new IllegalArgumentException("Not a valid .class file");
		this.minorVersion = in.readUnsignedShort();
		this.majorVersion = in.readUnsignedShort();
		int cPoolCount = in.readUnsignedShort();
		constantPool = new ConstantPool(cPoolCount);
		for(int i = 0; i < cPoolCount - 1; i++) {
			ConstantPoolEntry en = readConstantPoolEntry(constantPool, in);
			constantPool.setEntry(i, en);
			if(en.isDoubleEntry()) constantPool.setEntry(++i, null);
		}
		this.accessFlags = EnumFlagCompound.of(ClassAccessFlag.class, in.readUnsignedShort());
		this.thisClass = constantPool.getEntry(in.readUnsignedShort()).as(ConstantPoolClassEntry.class);
		this.superClass = constantPool.getEntry(in.readUnsignedShort()).as(ConstantPoolClassEntry.class);
		this.interfaces = new ConstantPoolClassEntry[in.readUnsignedShort()];
		for(int i = 0; i < interfaces.length; i++) {
			interfaces[i] = constantPool.getEntry(in.readUnsignedShort()).as(ConstantPoolClassEntry.class);
		}
		this.fields = new ClassField[in.readShort()];
		for(int i = 0; i < fields.length; i++) {
			int accFlags = in.readUnsignedShort();
			int nameIdx = in.readUnsignedShort();
			int descIdx = in.readUnsignedShort();
			Attribute[] attrs = new Attribute[in.readUnsignedShort()];
			for(int j = 0; j < attrs.length; j++) {
				attrs[j] = readAttribute(constantPool, in);
			}
			fields[i] = new ClassField(this, accFlags, nameIdx, descIdx, attrs);
		}
		this.methods = new ClassMethod[in.readShort()];
		for(int i = 0; i < methods.length; i++) {
			int accFlags = in.readUnsignedShort();
			int nameIdx = in.readUnsignedShort();
			int descIdx = in.readUnsignedShort();
			Attribute[] attrs = new Attribute[in.readUnsignedShort()];
			for(int j = 0; j < attrs.length; j++) {
				attrs[j] = readAttribute(constantPool, in);
			}
			methods[i] = new ClassMethod(this, accFlags, nameIdx, descIdx, attrs);
		}
		this.attributes = new Attribute[in.readUnsignedShort()];
		for(int i = 0; i < attributes.length; i++) {
			attributes[i] = readAttribute(constantPool, in);
		}
	}
	
	private ConstantPoolEntry readConstantPoolEntry(ConstantPool pool, DataInputStream in) throws IOException {
		int uByte = in.readUnsignedByte();
		ConstantPoolTag tag = ConstantPoolTag.getByValue(uByte);
		if(tag == null) {
			throw new IllegalArgumentException("Invalid constant pool (Invalid type: 0x" + Integer.toHexString(uByte) + ")");
		}
		switch(tag) {
			case CLASS:
				return new ConstantPoolClassEntry(constantPool, in.readUnsignedShort());
			case FIELD_REF:
				return new ConstantPoolFieldRefEntry(pool, in.readUnsignedShort(), in.readUnsignedShort());
			case METHOD_REF:
				return new ConstantPoolMethodRefEntry(pool, in.readUnsignedShort(), in.readUnsignedShort());
			case INTERFACE_METHOD_REF:
				return new ConstantPoolInterfaceMethodRefEntry(pool, in.readUnsignedShort(), in.readUnsignedShort());
			case STRING:
				return new ConstantPoolStringEntry(pool, in.readUnsignedShort());
			case INTEGER:
				return new ConstantPoolIntegerEntry(pool, in.readInt());
			case FLOAT:
				return new ConstantPoolFloatEntry(pool, in.readFloat());
			case LONG:
				return new ConstantPoolLongEntry(pool, in.readLong());
			case DOUBLE:
				return new ConstantPoolDoubleEntry(pool, in.readDouble());
			case NAME_AND_TYPE:
				return new ConstantPoolNameAndTypeEntry(pool, in.readUnsignedShort(), in.readUnsignedShort());
			case UTF_8:
				byte[] b = new byte[in.readUnsignedShort()];
				in.read(b);
				return new ConstantPoolUTF8Entry(pool, new String(b, StandardCharsets.UTF_8));
			case METHOD_HANDLE:
				return new ConstantPoolMethodHandleEntry(pool, in.readUnsignedByte(), in.readUnsignedShort());
			case METHOD_TYPE:
				return new ConstantPoolMethodTypeEntry(pool, in.readUnsignedShort());
			case INVOKE_DYNAMIC:
				return new ConstantPoolInvokeDynamicEntry(pool, in.readUnsignedShort(), in.readUnsignedShort());
		}
		throw new RuntimeException();
	}
	
	public Attribute readAttribute(ConstantPool pool, DataInputStream in) throws IOException {
		int aNameIdx = in.readUnsignedShort();
		byte[] info = new byte[in.readInt()];
		in.read(info);
		return parseAttribute(new AttributeRaw(this, aNameIdx, info));
	}
	
	private Attribute parseAttribute(AttributeRaw attr) throws IOException {
		DefaultAttributeType defType = DefaultAttributeType.getByName(attr.getNameString());
		if(defType == null) return attr;
		switch(defType) {
			case ANNOTATION_DEFAULT:
				break;
			case BOOTSTRAP_METHODS:
				break;
			case CODE:
				return new AttributeCode(this, attr.getInfo());
			case CONSTANT_VALUE:
				break;
			case DEPRECATED:
				break;
			case ENCLOSING_METHOD:
				break;
			case EXCEPTIONS:
				break;
			case INNER_CLASSES:
				break;
			case LINE_NUMBER_TABLE:
				break;
			case LOCAL_VARIABLE_TYPE:
				break;
			case LOCAL_VARIABLE_TYPE_TABLE:
				break;
			case RUNTIME_INVISIBLE_ANNOTATIONS:
				break;
			case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
				break;
			case RUNTIME_VISIBLE_ANNOTATIONS:
				break;
			case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
				break;
			case SIGNATURE:
				break;
			case SOURCE_DEBUG_EXCEPTION:
				break;
			case SOURCE_FILE:
				break;
			case STACK_MAP_TABLE:
				break;
			case SYNTHETIC:
				break;
		}
		return attr;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}
	
	public int getMinorVersion() {
		return minorVersion;
	}
	
	public ConstantPool getConstantPool() {
		return constantPool;
	}
	
	public EnumFlagCompound<ClassAccessFlag> getAccessFlags() {
		return accessFlags;
	}
	
	public ConstantPoolClassEntry getThisClass() {
		return thisClass;
	}
	
	public ConstantPoolClassEntry getSuperClass() {
		return superClass;
	}
	
	public ConstantPoolClassEntry[] getInterfaces() {
		return interfaces;
	}
	
	public ClassField[] getFields() {
		return fields;
	}
	
	public ClassMethod[] getMethods() {
		return methods;
	}
	
	public ClassMethod[] getMethods(String name) {
		return Arrays.stream(methods).filter(m -> m.getName().getValue().equals(name)).toArray(ClassMethod[]::new);
	}
	
	public Attribute[] getAttributes() {
		return attributes;
	}
	
	public Attribute getAttribute(String name) {
		return Arrays.stream(attributes).filter(a -> a.getNameString().equals(name)).findFirst().orElse(null);
	}
	
	public Attribute getAttribute(DefaultAttributeType type) {
		return Arrays.stream(attributes).filter(a -> a.getNameString().equals(type.getName())).findFirst().orElse(null);
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(accessFlags.getApplicable().stream().map(a -> a.getName()).collect(Collectors.joining(" "))).append(" class ")
			.append(new TypeDescriptor(thisClass.getName().getValue()).getFriendlyName()).append(" extends ").append(new TypeDescriptor(superClass.getName().getValue()).getFriendlyName());
		if(interfaces.length > 0) {
			res.append(" implements ").append(Arrays.stream(interfaces).map(i -> TypeDescriptor.parse(i.getName().getValue()).getFriendlyName()).collect(Collectors.joining(", ")));
		}
		res.append("\n\n");
		for(ClassField field : fields) res.append(field.toString()).append("\n");
		res.append("\n");
		for(ClassMethod method : methods) res.append(method.toString()).append("\n");
		return res.toString();
	}

}