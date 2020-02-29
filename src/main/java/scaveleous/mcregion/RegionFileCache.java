package scaveleous.mcregion;

/*
** 2011 January 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

// A simple cache and wrapper for efficiently multiple RegionFiles simultaneously.

import java.io.*;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;

public class RegionFileCache {
    private static final Map<File, Reference<RegionFile>> cache = new HashMap<File, Reference<RegionFile>>();

    private RegionFileCache() { }

    public static synchronized RegionFile getRegionFile(File basePath, int x, int z) {
        File regionDir = new File(basePath, "region");
        File file = new File(regionDir, "r." + (x >> 5) + "." + (z >> 5) + ".data");

        Reference<RegionFile> ref = cache.get(file);

        if (ref != null && ref.get() != null)
            return ref.get();

        if (!regionDir.exists())
            regionDir.mkdirs();

        RegionFile reg = new RegionFile(file);
        cache.put(file, new SoftReference<RegionFile>(reg));
        return reg;
    }

    public static synchronized void clear() {
        for (Reference<RegionFile> ref : cache.values()) {
            try {
                if (ref.get() != null)
                    ref.get().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cache.clear();
    }

    public static int getSizeDelta(File basePath, int x, int z) {
        RegionFile r = getRegionFile(basePath, x, z);
        return r.getSizeDelta();
    }

    public static DataInputStream getChunkDataInputStream(File basePath, int x, int z) {
        RegionFile r = getRegionFile(basePath, x, z);
        return r.getChunkDataInputStream(x & 31, z & 31);
    }

    public static DataOutputStream getChunkDataOutputStream(File basePath, int x, int z) {
        RegionFile r = getRegionFile(basePath, x, z);
        return r.getChunkDataOutputStream(x & 31, z & 31);
    }
}