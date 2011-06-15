/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebase;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import net.xy.codebasel.ByteArray;

/**
 * imgcache handler with resize and rotation support
 * 
 * @author xyan
 * 
 */
public class ImgCache {
    /**
     * min free disk capacity to check for
     */
    private static final long HDD_MIN_FREE = 1 * 1024 * 1024; // 1MB
    /**
     * minimum filesize to asume its correctly written
     */
    private static final long MIN_FILESIZE = 512;
    /**
     * root dir of cache
     */
    public final File basedir;
    public final URI baseuri;
    /**
     * enables readonly mode
     */
    public final boolean readOnly;
    /**
     * used to secure multithreading
     */
    private final HashMap<String, File> index = new HashMap<String, File>();

    /**
     * delegate constructor obmitting readonly false
     * 
     * @param basedir
     */
    public ImgCache(final File basedir) {
        this(basedir, false);
    }

    /**
     * main constructor
     * 
     * @param basedir
     * @param readOnly
     */
    public ImgCache(final File basedir, final boolean readOnly) {
        if (!basedir.isDirectory()) {
            throw new IllegalArgumentException("Basedir is no directory");
        }
        if (!readOnly && basedir.getUsableSpace() < HDD_MIN_FREE) {
            throw new IllegalStateException("Not enough free diskspace");
        }
        this.basedir = basedir;
        baseuri = basedir.toURI();
        this.readOnly = readOnly;
        initIndex(basedir);
    }

    /**
     * initializes the primary index
     * 
     * @param basedir
     * @param index
     */
    private void initIndex(final File basedir) {
        for (final File entry : basedir.listFiles()) {
            if (entry.isDirectory()) {
                initIndex(entry);
            } else {
                final URI relative = baseuri.relativize(entry.toURI());
                index.put(relative.toString(), entry);
            }
        }
    }

    /**
     * gets an existing or prepares an new cached image
     * 
     * @param identifier
     * @param width
     * @param height
     * @param rotation
     * @param quality
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalStateException
     */
    public CachedImage get(String identifier, final int width, final int height, final int rotation, final int quality)
            throws FileNotFoundException, IOException, IllegalStateException {
        identifier = prepareIdentifier(identifier);
        final File target = index.get(identifier);
        // file exists, paththrough
        if (target != null) {
            return new CachedImage(target);
        }
        // create image
        else {
            final BufferedImage result = Image.resize(getInput(identifier), new Dimension(width, height),
                    Image.Quality.HIGH);
            final CachedImage cImage = new CachedImage(identifier, new Date(), result);
            // file not exists, creating
            if (!readOnly) {
                final File cacheFile = new File(basedir.toString() + File.pathSeparator + identifier);
                if (cacheFile.createNewFile()) {
                    final FileOutputStream out = new FileOutputStream(cacheFile);
                    out.write(cImage.getData());
                    out.close();
                    index.put(identifier, cacheFile);
                } else {
                    throw new IllegalStateException(Debug.values("Unable to create cachefile", cacheFile));
                }
            }
            return cImage;
        }
    }

    /**
     * extends the identifier with all params
     * 
     * @param identifier
     * @param params
     * @return
     */
    private String prepareIdentifier(final String identifier, final Object... params) {
        final StringBuilder result = new StringBuilder(identifier);
        for (final Object entry : params) {
            result.append(entry);
        }
        return result.toString();
    }

    /**
     * opens an image from image base
     * 
     * @param identifier
     * @return
     * @throws IOException
     */
    private BufferedImage getInput(final String identifier) throws IOException {
        final File image = new File(identifier);
        if (!image.isFile()) {
            throw new FileNotFoundException(Debug.values("Target is no file", identifier));
        }
        if (image.length() < MIN_FILESIZE) {
            throw new IllegalStateException(Debug.values("File seems to be to small", identifier));
        }
        return ImageIO.read(image);
    }

    /**
     * get metadata from
     * get output from, and can write file
     * should store references
     * 
     * @author xyan
     * 
     */
    public static class CachedImage {
        /**
         * creation or modification date
         */
        public final Date date;
        /**
         * filename or resource identifier
         */
        public final String name;
        private final MessageDigest digest; // md5 digest
        private byte[] hash = null; // store hash
        private boolean checked = false; // flag to toggle computation
        private final ByteArray data; // image data
        /**
         * for memory images
         */
        private final ImageWriter writer;
        private final BufferedImage image;
        /**
         * for file based
         */
        private final File imageFile;

        /**
         * memory image constructor
         * 
         * @param name
         * @param date
         * @param image
         */
        public CachedImage(final String name, final Date date, final BufferedImage image) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            this.date = date;
            this.name = name;
            this.image = image;
            final String suffix = name.substring(name.lastIndexOf(".") + 1);
            final Iterator<ImageWriter> writerList = ImageIO.getImageWritersBySuffix(suffix);
            if (!writerList.hasNext()) {
                throw new IllegalStateException(Debug.values("No imagewriter available for suffix", suffix));
            }
            writer = writerList.next();
            // TODO add ouput image type
            data = new ByteArray(image.getHeight() * image.getWidth() * 4); // save assumption
            imageFile = null;
        }

        /**
         * init by diskfile
         * 
         * @param imageFile
         */
        public CachedImage(final File imageFile) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            date = new Date(imageFile.lastModified());
            name = imageFile.getName();
            image = null;
            writer = null;
            data = new ByteArray(image.getHeight() * image.getWidth() * 4); // save assumption
            this.imageFile = imageFile;
        }

        /**
         * computes data hash
         * 
         * @return
         * @throws IOException
         */
        public byte[] getHash() throws IOException {
            if (!checked) {
                hash = digest.digest(getData());
                checked = true;
            }
            return hash;
        }

        /**
         * returns data size
         * 
         * @return
         * @throws IOException
         */
        public int getSize() throws IOException {
            return getData().length;
        }

        /**
         * reads image data into memory
         * 
         * @return
         * @throws IOException
         */
        public byte[] getData() throws IOException {
            if (data == null) {
                checked = false;
                final OutputStream out = new OutputStream() {
                    @Override
                    public void write(final int b) throws IOException {
                        data.add((byte) b);
                    }

                    @Override
                    public void write(final byte[] b) throws IOException {
                        data.add(b);
                    }

                    @Override
                    public void write(final byte[] b, final int off, final int len) throws IOException {
                        data.add(b, off, len);
                    }
                };
                if (imageFile != null) {
                    net.xy.codebasel.File.getFile(imageFile, out);
                } else {
                    writer.setOutput(ImageIO.createImageOutputStream(out));
                    writer.write(image);
                    writer.dispose();
                    image.flush();
                }
            }
            return data.get();
        }
    }
}
