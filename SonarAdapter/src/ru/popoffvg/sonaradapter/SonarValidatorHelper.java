package ru.popoffvg.sonaradapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

public class SonarValidatorHelper {
	private final EditList diff;
	private final String moduleText;
	private final String sonarText;
	private final int lengthLineSeparator = System.getProperty("line.separator").length();

	public static SonarValidatorHelper create(String moduleText, String sonarText) {
		return new SonarValidatorHelper(moduleText, sonarText);
	}

	private SonarValidatorHelper(String moduleText, String sonarText) {
		this.moduleText = moduleText;
		this.sonarText = sonarText;
		this.diff = computeDiff();
	}

	// TODO Многострочные замечания?
	public TextRangeInfo info(int startLine, int startOffset, int length) {
		return new TextRangeInfo(startLine, startOffset, length);
	}

	private EditList computeDiff() {
		EditList diffList = new EditList();
		try {
			RawText rt2 = new RawText(moduleText.getBytes(StandardCharsets.UTF_8.toString()));
			RawText rt1 = new RawText(sonarText.getBytes(StandardCharsets.UTF_8.toString()));
			diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
		} catch (IOException e) {
			SonarAdapterPlugin.logError(e);
		}
		return diffList;
	}

	public class TextRangeInfo {
		private final boolean isRemoved;
		private final int startOffset;
		private final int length;

		private TextRangeInfo(int lineNumber, int startOffset, int length) {
			int shift = diff.stream()
					.filter(edit -> edit.getBeginA() <= lineNumber - 1)
					.mapToInt(edit -> calculateEditShift(edit, lineNumber - 1))
					.sum();
			Optional<String> snippetSonar = snippetByPosition(sonarText, lineNumber, startOffset, length);
			Optional<String> snippetModule = snippetByPosition(moduleText, lineNumber + shift, startOffset, length);

			this.startOffset = startOffsetFromTextRange(moduleText, lineNumber + shift, startOffset);
			this.length = length;
			this.isRemoved = !snippetModule.isPresent() 
					|| !snippetSonar.orElse("").equals(snippetModule.get());
		}

		/**
		 * @param text - текст модуля
		 * @param lineNumber - номер строки из редактора текста (минимальное значение 1)
		 * @param startOffset - начальная позиция замечания
		 * @param length - длина замечания
		 * @return
		 */
		private Optional<String> snippetByPosition(String text, int lineNumber, int startOffset, int length) {
			Optional<String> result;
			int startOffsetInText = startOffsetFromTextRange(text, lineNumber, startOffset);
			int endOffset = startOffsetInText + length;
			if (text.length() < endOffset) {
				result = Optional.empty();
			} else {
				result = Optional.of(text.substring(startOffsetInText, endOffset));
			}
			return result;
		}

		private int calculateEditShift(Edit edit, int lineNumber) {
			int limitLengthB = 0;
			int limitLengthA = 0;
			if(edit.getType() == Type.INSERT) {
				limitLengthB = edit.getEndB() - edit.getBeginB();
				limitLengthA = edit.getEndA() - edit.getBeginA();
			} else {
				limitLengthB = Math.min(edit.getEndB(), lineNumber) - edit.getBeginB();
				limitLengthA = Math.min(edit.getEndA(), lineNumber) - edit.getBeginA();
			}
			return limitLengthB - limitLengthA;
		}
		
		public boolean isRemoved() {
			return isRemoved;
		}

		/**
		 * @return число символом от начала текста. 
		 * Табуляция считается одним симоволом.Ц
		 */
		public int getStartOffset() {
			return startOffset;
		}

		public int getLength() {
			return length;
		}

		private int startOffsetFromTextRange(String text, int startLine, int startOffset) {
			int lineOffset = text.lines()
					.limit(startLine - 1)
					.mapToInt(l -> l.length() + lengthLineSeparator)
					.sum();
			return lineOffset + startOffset;
		}

	}
}
