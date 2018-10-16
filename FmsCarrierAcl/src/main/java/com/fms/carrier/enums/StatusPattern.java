package com.fms.carrier.enums;

import java.util.regex.Pattern;

public enum StatusPattern {
	P1(Pattern.compile("(.+) on vessel (.+) for (.+) On (.+) which sailed on (.+)")),
	P2(Pattern.compile("(.+) from vessel (.+) at (.+) On (.+)")),
	P3(Pattern.compile("(.+) for vessel (.+) at (.+) On (.+)")),
	P4(Pattern.compile("(.+) from (.+) from vessel (.+) On (.+)")),
	P5(Pattern.compile("(.+) at (.+) On (.+)")),
	P6(Pattern.compile("(.+) from (.+) On (.+)")),
	P7(Pattern.compile("(.+) for (.+) On (.+)"));
	
	private Pattern pattern;
	
	StatusPattern(Pattern pattern){
		this.pattern = pattern;
	}
	
	public Pattern getPattern(){
		return pattern;
	}
	
}
