package com.stericson.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.util.Log;

//no modifier, this means it is package-private. Only our internal classes can use this.
class Remounter {
	
    //-------------
    //# Remounter #
    //-------------

    /**
     * This will take a path, which can contain the file name as well,
     * and attempt to remount the underlying partition.
     * 
     * For example, passing in the following string:
     * "/system/bin/some/directory/that/really/would/never/exist"
     * will result in /system ultimately being remounted.
     * However, keep in mind that the longer the path you supply, the more work this has to do,
     * and the slower it will run.
     * 
     * @param file      file path
     * 
     * @param mountType mount type: pass in RO (Read only) or RW (Read Write)
     * 
     * @return          a <code>boolean</code> which indicates whether or not the partition
     *                  has been remounted as specified.
     */

    protected boolean remount(String file, String mountType) {
        //if the path has a trailing slash get rid of it.
        if (file.endsWith("/")) {
            file = file.substring(0, file.lastIndexOf("/"));
        }
        //Make sure that what we are trying to remount is in the mount list.
        boolean foundMount = false;
        while (!foundMount) {
            try {
                for (Mount mount : RootTools.getMounts()) {
                	RootTools.log(mount.mountPoint.toString());
    	        	
                    if (file.equals(mount.mountPoint.toString())) {
                        foundMount = true;
                        break;
                    }
                }
            }
            catch (Exception e) {
            	if (RootTools.debugMode) {
            		e.printStackTrace();
            	}
                return false;
            }
            if (!foundMount) {
                try {
                    file = (new File(file).getParent()).toString();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        Mount mountPoint = findMountPointRecursive(file);

        Log.i(InternalVariables.TAG, "Remounting " + mountPoint.mountPoint.getAbsolutePath() + " as " + mountType.toLowerCase());
        final boolean isMountMode = mountPoint.flags.contains(mountType.toLowerCase());

        if ( !isMountMode ) {
        	//grab an instance of the internal class
            InternalMethods.instance().doExec(new String[] {
                    String.format(
                            "mount -o remount,%s %s %s",
                            mountType.toLowerCase(),
                            mountPoint.device.getAbsolutePath(),
                            mountPoint.mountPoint.getAbsolutePath() )
                    });
            RootTools.log(String.format(
                        "mount -o remount,%s %s %s",
                        mountType.toLowerCase(),
                        mountPoint.device.getAbsolutePath(),
                        mountPoint.mountPoint.getAbsolutePath() ));
            mountPoint = findMountPointRecursive(file);
        }

        Log.i(InternalVariables.TAG, mountPoint.flags + " AND " +  mountType.toLowerCase());
        if ( mountPoint.flags.contains(mountType.toLowerCase()) ) {
        	RootTools.log(mountPoint.flags.toString());
            return true;
        } else {
        	RootTools.log(mountPoint.flags.toString());
            return false;
        }
    }
    
    private Mount findMountPointRecursive(String file) {
        try {
            ArrayList<Mount> mounts = RootTools.getMounts();
            for( File path = new File(file); path != null; ) {
                for(Mount mount : mounts ) {
                    if ( mount.mountPoint.equals( path )) {
                        return mount;
                    }
                }
            }
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        } catch (Exception e) {
			if (RootTools.debugMode) {
				e.printStackTrace();
			}
		}
        return null;
    }
}
