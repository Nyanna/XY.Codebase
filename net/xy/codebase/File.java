package net.xy.codebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class File {
    /**
     * buffersize for copy operations
     */
    public static final int COPY_BUFFER = 4 * 1024; // 4kb

    /**
     * appends an file to out
     * 
     * @param target
     * @param out
     * @throws IOException
     */
    public static void getFile(final java.io.File target, final OutputStream out) throws IOException {
        if (target.isFile()) {
            final FileInputStream in = new FileInputStream(target);
            final byte[] buffer = new byte[COPY_BUFFER];
            while (-1 != in.read(buffer)) {
                out.write(buffer);
            }
        } else {
            throw new FileNotFoundException(Debug.values("Target is no file", target));
        }
    }
}
