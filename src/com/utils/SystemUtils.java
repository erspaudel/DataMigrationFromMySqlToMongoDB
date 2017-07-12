package com.utils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class SystemUtils {

	public static final int MB = 1024 * 1024;
	public static final int GB = 1024 * 1024 * 1024;

	public static void printAllSystemUsage() {
		RAMUtils.printRAMUsage();
		SpaceUtils.printSpaceUsage();
		JVMUtils.printJVMUsage();
	}

	public static class RAMUtils {

		public static int getPhysicalMemorySize() {

			com.sun.management.OperatingSystemMXBean os = getOperatingSystemMXBean();
			return convertToMB(os.getTotalPhysicalMemorySize());
		}

		public static int getFreePhysicalMemorySize() {

			com.sun.management.OperatingSystemMXBean os = getOperatingSystemMXBean();
			return convertToMB(os.getFreePhysicalMemorySize());
		}

		private static com.sun.management.OperatingSystemMXBean getOperatingSystemMXBean() {

			com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
					.getOperatingSystemMXBean();
			return os;
		}

		public static void printRAMUsage() {

			System.err.println("\n \t\tRAM USAGE");
			System.out.println("Total Physical Memory Size (MB): " + getPhysicalMemorySize());
			System.out.println("Free Physical Memory Size (MB): " + getFreePhysicalMemorySize());
		}
	}

	public static int convertToMB(long value) {

		value = value / MB;
		return Integer.parseInt(value + "");
	}

	public static int convertToGB(long value) {


		
		value = value / GB;
		return Integer.parseInt(value + "");
	}
	
	public static String formatFileSize(long size) {
	    String hrSize = null;

	    double b = size;
	    double k = size/1024.0;
	    double m = ((size/1024.0)/1024.0);
	    double g = (((size/1024.0)/1024.0)/1024.0);
	    double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

	    DecimalFormat dec = new DecimalFormat("0.00");

	    if ( t>1 ) {
	        hrSize = dec.format(t).concat(" TB");
	    } else if ( g>1 ) {
	        hrSize = dec.format(g).concat(" GB");
	    } else if ( m>1 ) {
	        hrSize = dec.format(m).concat(" MB");
	    } else if ( k>1 ) {
	        hrSize = dec.format(k).concat(" KB");
	    } else {
	        hrSize = dec.format(b).concat(" Bytes");
	    }

	    return hrSize;
	}

	public static class SpaceUtils {

		public static int getTotalSpace() {

			File[] roots = File.listRoots();

			for (File root : roots) {
				System.out.println(root.getTotalSpace());
				return convertToGB(root.getTotalSpace());
			}

			return 0;
		}

		public static int getFreeSpace() {
			File[] roots = File.listRoots();

			for (File root : roots) {
				return convertToGB(root.getFreeSpace());
			}

			return 0;
		}

		public static int getUsableSpace() {

			File[] roots = File.listRoots();

			for (File root : roots) {
				return convertToGB(root.getUsableSpace());
			}

			return 0;
		}

		public static void printSpaceUsage() {

			System.err.println("\n \t\tSPACE USAGE");

			System.out.println("Total space (GB): " + getTotalSpace());
			System.out.println("Free space (GB): " + getFreeSpace());
			System.out.println("Usable space (GB): " + getUsableSpace());
		}
	}

	public static class JVMUtils {

		public static int getUsedMemory() {

			Runtime runtime = Runtime.getRuntime();
			return convertToMB(runtime.totalMemory() - runtime.freeMemory());
		}

		public static int getFreeMemory() {

			Runtime runtime = Runtime.getRuntime();
			return convertToMB(runtime.freeMemory());
		}

		public static int getTotalMemory() {

			Runtime runtime = Runtime.getRuntime();
			return convertToMB(runtime.totalMemory());
		}

		public static int getMaxMemory() {

			Runtime runtime = Runtime.getRuntime();
			return convertToMB(runtime.maxMemory());
		}

		public static void printJVMUsage() {

			System.err.println("\n \t\tJVM USAGE");

			System.out.println("Used Memory: " + getUsedMemory());
			System.out.println("Free Memory:" + getFreeMemory());
			System.out.println("Total Memory:" + getTotalMemory());
			System.out.println("Max Memory:" + getMaxMemory());
		}
	}

}
