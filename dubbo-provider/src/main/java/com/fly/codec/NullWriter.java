package com.fly.codec;

import java.io.Writer;


class NullWriter extends Writer {
	@Override
	public void close() {
		// blank
	}

	@Override
	public void flush() {
		// blank
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		// blank
	}
}