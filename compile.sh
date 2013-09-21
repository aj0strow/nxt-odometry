source clean.sh;
echo -n "compiling project... ";
find src -type f -name *.java | xargs nxjc -d classes;
cd classes && nxjlink -o ../Lab1.nxj Lab1;
cd .. && echo "complete!"