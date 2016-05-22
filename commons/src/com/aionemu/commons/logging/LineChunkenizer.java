/**
 * Copyright 2014 AnjLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aionemu.commons.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;


/**
 * Based on the work of <a href="https://github.com/anjlab/logback-hipchat-appender">Dmitry Gusev</a>
 * 
 * @author Dmitry Gusev
 */
public class LineChunkenizer {

	private final BufferedReader reader;

	private final int maxChunkSize;

	public static interface ChunkCallback {

		void gotChunk(String chunk, boolean hasMoreChunks);
	}

	public LineChunkenizer(String input, int maxChunkSize) {
		this.reader = new BufferedReader(new StringReader(input));
		this.maxChunkSize = maxChunkSize;
	}

	public void chunkenize(ChunkCallback chunkCallback) {
		StringBuilder chunk = newChunkBuffer();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				int nextLength = chunk.length() + line.length() + (chunk.length() == 0 ? 0 : "\n".length());

				if (nextLength <= maxChunkSize) {
					if (chunk.length() > 0) {
						chunk.append('\n');
					}
					chunk.append(line);
				} else {
					// Current line doesn't fit into current chunk

					if (chunk.length() > 0) {
						chunkCallback.gotChunk(chunk.toString(), true);
						chunk = newChunkBuffer();
					}

					// Current chunk is empty now

					if (line.length() <= maxChunkSize) {
						chunk.append(line);
					} else {
						// Current line doesn't fit to an empty chunk
						// => split current line by parts and make chunks of all those parts
						// before moving to next line

						int beginIndex = 0;
						while (beginIndex < line.length()) {
							int endIndex = Math.min(beginIndex + maxChunkSize, line.length());
							chunkCallback.gotChunk(line.substring(beginIndex, endIndex), endIndex != line.length() || hasMoreDataInReader());
							beginIndex += maxChunkSize;
						}
					}
				}
			}

			if (chunk.length() > 0) {
				chunkCallback.gotChunk(chunk.toString(), false);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean hasMoreDataInReader() throws IOException {
		reader.mark(1);
		try {
			return reader.read() != -1;
		} finally {
			reader.reset();
		}
	}

	private StringBuilder newChunkBuffer() {
		return new StringBuilder(maxChunkSize);
	}
}
