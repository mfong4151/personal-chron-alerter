# personal-chron-alerter

Current state: needs a chatgpt integration

## TODO

- Integrate chatgpt api, LOE 1 day 
- E2E test of api, sending alert 1 day 
- Migrate to raspberry pi 1 day -> needs purchase of hardware

### OpenAi documentation 
https://github.com/openai/openai-java


### Billing dashboard 
https://platform.openai.com/usage


### Ongoing chat
https://chatgpt.com/c/69249324-6a0c-8332-8f99-a0debaaced14


### Reboot steps
🔁 E2E Reboot Flow (from your laptop)
1️⃣ SSH into the Pi

From your local terminal (WSL / PowerShell / terminal where SSH works):

Using IP (most reliable):
ssh mfong415@192.168.1.47

Or hostname (if resolution works):
ssh mfong415@maxs-pi.local


2 Check the status

```bash
sudo systemctl status chron
```

2️⃣ Reboot the Pi

Once logged in:

sudo reboot


You’ll immediately get disconnected — that’s normal.


🔍 Post-reboot health check (optional but smart)

Run on the Pi:

uptime
hostname
hostname -I
systemctl status chron


Logs:

journalctl -u chron -n 30

2) How do I update the JAR?

Simple flow:

On your dev machine:
./gradlew shadowJar
scp build/libs/*-all.jar mfong415@<PI_IP>:/home/mfong415/chron/chron.jar

On the Pi:
sudo systemctl restart chron

