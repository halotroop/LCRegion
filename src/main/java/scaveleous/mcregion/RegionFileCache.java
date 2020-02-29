package scaveleous.mcregion;

import java.io.*;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;

import org.joml.Vector3i;

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
public class RegionFileCache
{
	private static final Map<File, Reference<RegionFile>> cache = new HashMap<File, Reference<RegionFile>>();

	private RegionFileCache()
	{}

	public static synchronized RegionFile getRegionFile(File basePath, Vector3i pos)
	{
		File regionDir = new File(basePath, "region");
		File file = new File(regionDir, "r." + (pos.x >> 5) + "." + (pos.y >> 5) + ".data");
		Reference<RegionFile> ref = cache.get(file);
		if (ref != null && ref.get() != null)
			return ref.get();
		if (!regionDir.exists())
			regionDir.mkdirs();
		RegionFile reg = new RegionFile(file);
		cache.put(file, new SoftReference<RegionFile>(reg));
		return reg;
	}

	public static synchronized void clear()
	{
		for (Reference<RegionFile> ref : cache.values())
		{
			try
			{
				if (ref.get() != null)
					ref.get().close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		cache.clear();
	}

	public static int getSizeDelta(File basePath, Vector3i pos)
	{
		RegionFile r = getRegionFile(basePath, pos);
		return r.getSizeDelta();
	}

	public static DataInputStream getChunkDataInputStream(File basePath, Vector3i pos)
	{
		RegionFile r = getRegionFile(basePath, pos);
		return r.getChunkDataInputStream(pos.x & 31, pos.y & 31, pos.z & 31);
	}

	public static DataOutputStream getChunkDataOutputStream(File basePath, Vector3i pos)
	{
		RegionFile r = getRegionFile(basePath, pos);
		return r.getChunkDataOutputStream(pos.x & 31, pos.y & 31, pos.z & 31);
	}
}