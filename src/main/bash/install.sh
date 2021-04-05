#!/bin/bash
if [[ "$EUID" -ne "0" ]]; then
  echo "you must be root";
  exit;
fi
#apt-get install -y openjdk-11-jre wget;
MIRROR="quelafraicheur.info/download/";
OUT_PATH="/usr/share/mars-vm";
mkdir -p $OUT_PATH;
echo "$OUT_PATH was created";
cd $OUT_PATH;
echo "path successfully changed";
rm mars-1.0-SNAPSHOT.jar;
wget https://$MIRROR/mars-1.0-SNAPSHOT.jar;
echo "wget successfully";
cat > /lib/systemd/system/mars-vm.service <<EOF
[Unit]
Description=Mars Vm Server.

[Service]
Type=simple
Restart=always
RestartSec=1
User=root
WorkingDirectory=$OUT_PATH/
ExecStart=/usr/bin/java -jar $OUT_PATH/mars-1.0-SNAPSHOT.jar

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload;
echo "mars-VM is installed";
