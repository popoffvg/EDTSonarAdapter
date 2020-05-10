package ru.popoffvg.sonarqube;

public class Issue {
	String key;
	String rule;
	public String getRule() {
		return rule;
	}

	String severity;
	String component;
	String project;
	public TextRange getTextRange() {
		return textRange;
	}

	public String getMessage() {
		return message;
	}

	int line;
	String hash;
	TextRange textRange;
	String status;
	String message;
	String author;
	
	public class TextRange{
		public int getStartLine() {
			return startLine;
		}
		public int getEndLine() {
			return endLine;
		}
		public int getStartOffset() {
			return startOffset;
		}
		public int getEndOffset() {
			return endOffset;
		}
		int startLine;
		int endLine;
		int startOffset;
		int endOffset;
	}
}
