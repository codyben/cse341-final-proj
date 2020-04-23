cd cob322;
echo "CODY NOTE: MAKE SURE TO COMPILE ON SUNLAB";
cd .. && ./compile.sh && cd cob322 && jar cfmv ../cob322.jar Manifest.txt *.class;