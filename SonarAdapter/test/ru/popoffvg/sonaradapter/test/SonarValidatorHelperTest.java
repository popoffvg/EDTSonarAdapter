package ru.popoffvg.sonaradapter.test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import ru.popoffvg.sonaradapter.SonarValidatorHelper;
import ru.popoffvg.sonaradapter.SonarValidatorHelper.TextRangeInfo;

public class SonarValidatorHelperTest {

	@Test
	public void testCalculateOffset() throws IOException {
		
	}
	
	// TODO пограничные случаи удаления: полностью диапазон
	@Test
	public void testDeleteRange() throws IOException {
		String original = Files.readString(new File("resources/test/fixture.txt").toPath(), StandardCharsets.UTF_8);
		String modify = Files.readString(new File("resources/test/fixture_deleteText.txt").toPath(), StandardCharsets.UTF_8);
		
		SonarValidatorHelper helper = SonarValidatorHelper.create(
					modify,
					original);
		assertEquals(true, helper.info(4, 12, 37).isRemoved());
	}
	
	@Test
	public void testEditRange() throws IOException {
		String original = Files.readString(new File("resources/test/fixture.txt").toPath(), StandardCharsets.UTF_8);
		String modify = Files.readString(new File("resources/test/fixture_edit.txt").toPath(), StandardCharsets.UTF_8);
		
		SonarValidatorHelper helper = SonarValidatorHelper.create(
					modify,
					original);
		assertEquals(true, helper.info(4, 11, 37).isRemoved());
		assertEquals(true, helper.info(8, 11, 37).isRemoved()); // out of range
	}
	
	@Test
	public void testNoChange() throws IOException {
		String original = Files.readString(new File("resources/test/fixture.txt").toPath(), StandardCharsets.UTF_8);
		
		SonarValidatorHelper helper = SonarValidatorHelper.create(
					original,
					original);
		assertEquals(false, helper.info(4, 12, 37).isRemoved());
	}
	
	@Test
	public void testInsertLine() throws IOException {
		String original = Files.readString(new File("resources/test/fixture.txt").toPath(), StandardCharsets.UTF_8);
		String modify = Files.readString(new File("resources/test/fixture_addLine.txt").toPath(), StandardCharsets.UTF_8);
		
		SonarValidatorHelper helper = SonarValidatorHelper.create(
					modify,
					original);
		TextRangeInfo info = helper.info(4, 8, 37);
		
		assertEquals(false, info.isRemoved());
		assertEquals(32, info.getStartOffset());
		assertEquals(37, info.getLength());
	}
	
	@Test
	public void testDeleteLine() {
		
	}
}
