#Assumes that you have mounted the / directory on a Sunlab machine using cifs/fuse.
#https://confluence.cc.lehigh.edu/display/hpc/Linux+Software you can adapt from this, or use sshfs:
#sshfs xyz123@sunlab.cse.lehigh.edu:/ <mount_point>

#unmount like so: fusermount -u <mount_point>


#https://paste.lehigh.edu/pwxxp7oky

#below, I used /mnt/sunexec as my mount point.
SUNLAB_JDK_VERS="jdk1.8.0_102"
SUNLAB_JAVA_INSTALL="/mnt/sunexec/usr/java/${SUNLAB_JDK_VERS}/bin/"
SUNLAB_JAVA="${SUNLAB_JAVA_INSTALL}java"
SUNLAB_JAVAC="${SUNLAB_JAVA_INSTALL}javac"
SUNLAB_JAR="/mnt/sunexec/usr/java/${SUNLAB_JDK_VERS}/bin/jar"
LEHIGH_ID="xyz123"
JAR_NAME="${LEHIGH_ID}.jar"

cd Project;

$SUNLAB_JAVAC *.java;

$SUNLAB_JAR cfmv ../$JAR_NAME Manifest.txt *.class;

STATUS=$?

cd ..;

if [[ $STATUS -eq 0 ]]; then

	$SUNLAB_JAVA -jar $JAR_NAME
	exit $? #trap the return code of our Java process on exit.

fi