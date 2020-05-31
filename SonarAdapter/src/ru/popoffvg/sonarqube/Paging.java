package ru.popoffvg.sonarqube;

class Paging {
	int pageIndex;
	int pageSize;
	public int numberOfPages() {
		return pageSize == 0 ? 1 : Math.max(1, total / pageSize);
	}
	int total;
}
