source clean.sh;
echo -n "compiling project... ";
find src -type f -name *.java | xargs nxjc -d classes;
cd classes && nxjlink -o ../Lab2.nxj Lab2;
cd .. && echo "complete!"