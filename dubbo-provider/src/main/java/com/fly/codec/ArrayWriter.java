package com.fly.codec;

import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * 专为产生抛出对象的堆栈的封装
 *
 * @author nikodu
 *
 */
final class ArrayWriter extends PrintWriter {
	private ArrayList<String> alist;

	public ArrayWriter() {
		super(new NullWriter());
		alist = new ArrayList<String>();
	}

	@Override
	public void print(char[] chars) {
		alist.add(new String(chars));
	}

	@Override
	public void print(Object o) {
		alist.add(o.toString());
	}

	@Override
	public void print(String s) {
		alist.add(s);
	}

	@Override
	public void println(char[] chars) {
		alist.add(new String(chars));
	}

	@Override
	public void println(Object o) {
		alist.add(o.toString());
	}

	@Override
	public void println(String s) {
		alist.add(s);
	}

	public String[] toStringArray() {
		int len = alist.size();
		String[] result = new String[len];
		for (int i = 0; i < len; i++) {
			result[i] = alist.get(i);
		}
		return result;
	}

	@Override
	public void write(char[] chars) {
		alist.add(new String(chars));
	}

	@Override
	public void write(char[] chars, int off, int len) {
		alist.add(new String(chars, off, len));
	}

	@Override
	public void write(String s) {
		alist.add(s);
	}

	@Override
	public void write(String s, int off, int len) {
		alist.add(s.substring(off, off + len));
	}
}

