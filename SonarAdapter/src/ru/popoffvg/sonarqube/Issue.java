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
	public SonarTextRange getTextRange() {
		return textRange;
	}

	public String getMessage() {
		return message;
	}

	int line;
	String hash;
	SonarTextRange textRange;
	String status;
	String message;
	String author;
	
	public class SonarTextRange{
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
