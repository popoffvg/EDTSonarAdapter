package ru.popoffvg.sonarqube;

import java.util.ArrayList;

public class SearchResponce {
	int total;
	int p;
	int ps;
	Paging paging;
	int effortTotal;
	int debtTotal;
	ArrayList<Issue> issues;
	public ArrayList<Issue> getIssues() {
		return issues;
	}
}
