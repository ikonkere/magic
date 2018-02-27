package com.roscap.mw.transport.correlation;

@Deprecated
public enum CorrelationType {
	UNIQUE(c -> c);
	
	private interface Generator {
		String generate(String base);
	}
	
	private final Generator generator;
	
	private CorrelationType(Generator arg0) {
		generator = arg0;
	}
	
	public String replyTo(String correlation) {
		return generator.generate(correlation);
	}
}
