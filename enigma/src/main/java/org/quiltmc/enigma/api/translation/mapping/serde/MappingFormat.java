package org.quiltmc.enigma.api.translation.mapping.serde;

import com.google.common.io.MoreFiles;
import org.quiltmc.enigma.api.ProgressListener;
import org.quiltmc.enigma.api.translation.mapping.EntryMapping;
import org.quiltmc.enigma.api.translation.mapping.MappingDelta;
import org.quiltmc.enigma.api.translation.mapping.serde.enigma.EnigmaMappingsReader;
import org.quiltmc.enigma.api.translation.mapping.serde.enigma.EnigmaMappingsWriter;
import org.quiltmc.enigma.api.translation.mapping.serde.proguard.ProguardMappingsReader;
import org.quiltmc.enigma.api.translation.mapping.serde.srg.SrgMappingsWriter;
import org.quiltmc.enigma.api.translation.mapping.serde.tinyv2.TinyV2Reader;
import org.quiltmc.enigma.api.translation.mapping.serde.tinyv2.TinyV2Writer;
import org.quiltmc.enigma.api.translation.mapping.tree.EntryTree;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum MappingFormat {
	ENIGMA_FILE(EnigmaMappingsWriter.FILE, EnigmaMappingsReader.FILE),
	ENIGMA_DIRECTORY(EnigmaMappingsWriter.DIRECTORY, EnigmaMappingsReader.DIRECTORY),
	ENIGMA_ZIP(EnigmaMappingsWriter.ZIP, EnigmaMappingsReader.ZIP),
	TINY_V2(new TinyV2Writer("intermediary", "named"), new TinyV2Reader()),
	SRG_FILE(SrgMappingsWriter.INSTANCE, null),
	PROGUARD(null, ProguardMappingsReader.INSTANCE);

	private final MappingsWriter writer;
	private final MappingsReader reader;

	MappingFormat(MappingsWriter writer, MappingsReader reader) {
		this.writer = writer;
		this.reader = reader;
	}

	public void write(EntryTree<EntryMapping> mappings, Path path, MappingSaveParameters saveParameters) {
		this.write(mappings, MappingDelta.added(mappings), path, ProgressListener.none(), saveParameters);
	}

	public void write(EntryTree<EntryMapping> mappings, Path path, ProgressListener progressListener, MappingSaveParameters saveParameters) {
		this.write(mappings, MappingDelta.added(mappings), path, progressListener, saveParameters);
	}

	public void write(EntryTree<EntryMapping> mappings, MappingDelta<EntryMapping> delta, Path path, ProgressListener progressListener, MappingSaveParameters saveParameters) {
		if (this.writer == null) {
			throw new IllegalStateException(this.name() + " does not support writing");
		}

		this.writer.write(mappings, delta, path, progressListener, saveParameters);
	}

	/**
	 * Reads the provided path and returns it as a tree of mappings.
	 * @param path the path to read
	 * @return a tree of every read mapping
	 * @throws IOException when there's an I/O error reading the files
	 * @throws MappingParseException when there's an issue with the content of a mapping file
	 */
	public EntryTree<EntryMapping> read(Path path) throws IOException, MappingParseException {
		if (this.reader == null) {
			throw new IllegalStateException(this.name() + " does not support reading");
		}

		return this.reader.read(path, ProgressListener.none());
	}

	/**
	 * Reads the provided path and returns it as a tree of mappings.
	 * @param path the path to read
	 * @param progressListener a progress listener to be used for displaying the progress to the user
	 * @return a tree of every read mapping
	 * @throws IOException when there's an I/O error reading the files
	 * @throws MappingParseException when there's an issue with the content of a mapping file
	 */
	public EntryTree<EntryMapping> read(Path path, ProgressListener progressListener) throws IOException, MappingParseException {
		if (this.reader == null) {
			throw new IllegalStateException(this.name() + " does not support reading");
		}

		return this.reader.read(path, progressListener);
	}

	@Nullable
	public MappingsWriter getWriter() {
		return this.writer;
	}

	@Nullable
	public MappingsReader getReader() {
		return this.reader;
	}

	/**
	 * Determines the mapping format of the provided file. Checks all formats, and returns {@link #PROGUARD} if none match.
	 * @param file the file to analyse
	 * @apiNote Any directory is considered to be the {@link #ENIGMA_DIRECTORY} format.
	 * Proguard does not have an explicit file extension, so it is the fallback.
	 * @return the mapping format of the file.
	 */
	public static MappingFormat parseFromFile(Path file) {
		if (Files.isDirectory(file)) {
			return ENIGMA_DIRECTORY;
		} else {
			switch (MoreFiles.getFileExtension(file).toLowerCase()) {
				case "zip" -> {
					return ENIGMA_ZIP;
				}
				case "mapping", "mappings" -> {
					return ENIGMA_FILE;
				}
				case "tiny" -> {
					return TINY_V2;
				}
				case "tsrg" -> {
					return SRG_FILE;
				}
				default -> {
					return PROGUARD;
				}
			}
		}
	}
}
