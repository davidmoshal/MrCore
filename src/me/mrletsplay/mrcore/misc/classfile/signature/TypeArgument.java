package me.mrletsplay.mrcore.misc.classfile.signature;

import me.mrletsplay.mrcore.misc.CharReader;

public class TypeArgument {
	
	private boolean any;
	private Character wildcardIndicator;
	private ReferenceTypeSignature typeSignature;
	
	public TypeArgument() {
		this.any = true;
	}
	
	public TypeArgument(Character wildcardIndicator, ReferenceTypeSignature typeSignature) {
		this.any = false;
		this.wildcardIndicator = wildcardIndicator;
		this.typeSignature = typeSignature;
	}

	public boolean isAny() {
		return any;
	}
	
	public Character getWildcardIndicator() {
		return wildcardIndicator;
	}
	
	public ReferenceTypeSignature getTypeSignature() {
		return typeSignature;
	}
	
	public boolean isExtends() {
		return wildcardIndicator != null && wildcardIndicator.equals('+');
	}
	
	public boolean isSuper() {
		return wildcardIndicator != null && wildcardIndicator.equals('-');
	}
	
	public boolean isExact() {
		return !any && wildcardIndicator == null;
	}
	
	public Wildcard getWildcard() {
		if(any) return Wildcard.ANY;
		if(wildcardIndicator == null) return Wildcard.NONE;
		return isExtends() ? Wildcard.EXTENDS : Wildcard.SUPER;
	}
	
	public static TypeArgument read(CharReader reader) {
		char c = reader.next();
		if(c == '*') return new TypeArgument();
		Character wc = c == '+' || c == '-' ? c : null;
		if(wc == null) reader.revert();
		return new TypeArgument(wc, ReferenceTypeSignature.read(reader));
	}
	
	public static enum Wildcard {
		
		ANY,
		EXTENDS,
		SUPER,
		NONE;
		
	}

}
