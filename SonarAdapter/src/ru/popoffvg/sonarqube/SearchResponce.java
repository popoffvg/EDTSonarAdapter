package ru.popoffvg.sonarqube;

import java.util.ArrayList;
import java.util.List;

public class SearchResponce {
	int total;
	int p;
	int ps;
	Paging paging;
	int effortTotal;
	int debtTotal;
	ArrayList<Issue> issues;
	
	public List<Issue> getIssues() {
		return issues;
	}
	
	public int numberOfPages() {
		return paging.numberOfPages();
	}
}
