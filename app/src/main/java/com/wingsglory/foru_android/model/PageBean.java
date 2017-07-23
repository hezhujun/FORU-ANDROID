package com.wingsglory.foru_android.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by hezhujun on 2017/7/18.
 */
public class PageBean<T> {
    private static final int MAX_ROWS = Integer.MAX_VALUE;
    public static final int DEFAULT_ROWS_OF_PAGE = 20;

    private int totalRows = MAX_ROWS;
    private int rows = DEFAULT_ROWS_OF_PAGE;
    private int page = 1;
    private List<T> beans;

    public PageBean() {
    }

    public PageBean(int totalRows, int rows, int page, List<T> beans) {
        this.totalRows = totalRows;
        this.rows = rows;
        this.page = page;
        if (beans != null) {
            if (beans.size() > rows) {
                throw new RuntimeException("PageBean长度超出范围");
            } else {
                this.beans = beans;
            }
        }
    }

    public PageBean(int totalRows, int rows, int page) {
        this(totalRows, rows, page, null);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<T> getBeans() {
        return beans;
    }

    public void setBeans(List<T> beans) {
        this.beans = beans;
    }

    @Override
    public String toString() {
        return "PageBean{" +
                "totalRows=" + totalRows +
                ", rows=" + rows +
                ", page=" + page +
                ", beans=" + showList(beans) +
                '}';
    }

    private String showList(List list) {
        if (list == null) {
            return "[]";
        }
        return Arrays.toString(list.toArray());
    }

    public int getTotalPages() {
        if ((totalRows % rows) == 0) {
            return totalRows / rows;
        } else {
            return totalRows / rows + 1;
        }
    }

    public int size() {
        if (beans == null || beans.size() == 0) {
            return 0;
        } else {
            return beans.size();
        }
    }

    public boolean hasNext() {
        return getTotalPages() > page;
    }

    public boolean hasBefore() {
        return page > 1;
    }
}
