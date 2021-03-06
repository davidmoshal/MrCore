package me.mrletsplay.mrcore.misc.classfile.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.misc.EnumFlagCompound;
import me.mrletsplay.mrcore.misc.PrimitiveType;
import me.mrletsplay.mrcore.misc.classfile.ByteCode;
import me.mrletsplay.mrcore.misc.classfile.ClassFile;
import me.mrletsplay.mrcore.misc.classfile.Instruction;
import me.mrletsplay.mrcore.misc.classfile.InstructionInformation;
import me.mrletsplay.mrcore.misc.classfile.MethodAccessFlag;
import me.mrletsplay.mrcore.misc.classfile.MethodDescriptor;

public class ByteCodeUtils {
	
	public static ByteCode parse(ClassFile classFile, String... code) {
		List<InstructionInformation> inf = new ArrayList<>();
		for(String c : code) inf.add(parse(classFile, c));
		return ByteCode.of(inf);
	}
	
	public static InstructionInformation parse(ClassFile classFile, String code) {
		String[] spl = code.split(" ");
		Instruction instr = Instruction.valueOf(spl[0].toUpperCase());
		
		List<String> args = Arrays.stream(spl).skip(1).collect(Collectors.toList());
		List<Byte> info = new ArrayList<>();
		
		switch(instr) {
			case INVOKESTATIC:
			case INVOKESPECIAL:
			case INVOKEVIRTUAL:
			{
				if(args.size() != 3) throw new IllegalArgumentException("Need 3 args for " + instr + " (clz, mth, sig)");
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendMethodRef(classFile, ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))), ClassFileUtils.getOrAppendNameAndType(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)), ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)))));
				for(byte b : bs) info.add(b);
				break;
			}
//			case INVOKEDYNAMIC: // TODO: Fix invokedynamic, use InvokeDynamic cp entry
//			{
//				if(args.size() != 3) throw new IllegalArgumentException("Need 3 args for " + instr + " (clz, mth, sig)");
//				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendMethodRef(classFile, ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))), ClassFileUtils.getOrAppendNameAndType(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)), ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)))));
//				for(byte b : bs) info.add(b);
//				info.add((byte) 0);
//				info.add((byte) 0);
//				break;
//			}
			case INVOKEINTERFACE:
			{
				if(args.size() != 3) throw new IllegalArgumentException("Need 3 args for " + instr + " (clz, mth, sig)");
				String
					className = args.remove(0),
					mthName = args.remove(0),
					mthSig = args.remove(0);
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendInterfaceMethodRef(classFile, ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, className)), ClassFileUtils.getOrAppendNameAndType(classFile, ClassFileUtils.getOrAppendUTF8(classFile, mthName), ClassFileUtils.getOrAppendUTF8(classFile, mthSig))));
				for(byte b : bs) info.add(b);
				
				int count = Arrays.stream(new MethodDescriptor(mthName, EnumFlagCompound.noneOf(MethodAccessFlag.class), mthSig).getParameterDescriptors())
						.mapToInt(p -> (p.getParameterType().getPrimitiveType() == PrimitiveType.DOUBLE || p.getParameterType().getPrimitiveType() == PrimitiveType.DOUBLE) ? 2 : 1)
						.sum();
				
				info.add((byte) (count + 1));
				info.add((byte) 0);
				
				break;
			}
			case NEW:
			{
				if(args.size() != 1) throw new IllegalArgumentException("Need 1 arg for " + instr + " (clz)");
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))));
				for(byte b : bs) info.add(b);
				break;
			}
			case CHECKCAST:
			{
				if(args.size() != 1) throw new IllegalArgumentException("Need 1 arg for " + instr + " (clz)");
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))));
				for(byte b : bs) info.add(b);
				break;
			}
			case PUTFIELD:
			case PUTSTATIC:
			{
				if(args.size() != 3) throw new IllegalArgumentException("Need 3 args for " + instr + " (clz, fld, type)");
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendFieldRef(classFile, ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))), ClassFileUtils.getOrAppendNameAndType(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)), ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)))));
				for(byte b : bs) info.add(b);
				break;
			}
			case GETFIELD:
			case GETSTATIC:
			{
				if(args.size() != 3) throw new IllegalArgumentException("Need 3 args for " + instr + " (clz, fld, type)");
				byte[] bs = ClassFileUtils.getShortBytes(ClassFileUtils.getOrAppendFieldRef(classFile, ClassFileUtils.getOrAppendClass(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0))), ClassFileUtils.getOrAppendNameAndType(classFile, ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)), ClassFileUtils.getOrAppendUTF8(classFile, args.remove(0)))));
				for(byte b : bs) info.add(b);
				break;
			}
			default:
				break;
		}
		
		args.forEach(a -> info.add(Byte.valueOf(a)));
		
		byte[] bs = new byte[info.size()];
		for(int i = 0; i < bs.length; i++) bs[i] = info.get(i);
		return new InstructionInformation(instr, bs);
	}

}
