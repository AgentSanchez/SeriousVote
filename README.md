<h1>Serious Vote</h1>

<img src="http://i.imgur.com/sf3L4Wu.png" width=800px height=250px style="float: right;">

----------
:octocat:[GitHub](https://github.com/curscascis/SeriousVote) | [:computer:bStats](https://bstats.org/plugin/sponge/SeriousVote) | [:question:Wiki](https://github.com/curscascis/SeriousVote/wiki) | [:floppy_disk: Download - Latest Stable](https://raw.githubusercontent.com/curscascis/SeriousVote/master/build/libs/seriousvote-4.8.3.jar) | [:moneybag:Donate](https://paypal.me/iamabanana)


***Why not go anywhere else?***

This is the best nuVotifier listener you'll ever use. Players not online? No problem, it will rack up their votes until next time they join, and don't worry it won't spam the public chat 50 times. NuVotifier also runs it's listener Async to make it compatible with BungeeCord. I've implemented a work around so commands are run from the main thread. If that wasn't enough,  cut the silence and motivate your players to vote by setting up the milestone rewards! Like dailies but for your addicting heroine-like server, How could Blizzard ever compete with you? Even better you can make your own **Custom** milestones, want to make a record for 3,20,17 votes you can do that! Want to make one for 1337 votes, you can do that too!

The whole Loot system has been reworked to introduce the new extremely flexible Loot Tables and table sets. These allow you to have the reward system choose from multiple sets of rewards allowing you to both re-use some rewards and organize and separate how you use them. Even better you can now for normal votes run more than one command.

**Need help?**
Check out out wiki, the link is above.
Oh....So you still need help? Check out the discord, I'll help you figure it out.

**Requirements**
[SpongeForge/Vanilla](https://www.spongepowered.org/downloads)
[NuVotifier - Official!](https://github.com/NuVotifier/NuVotifier/releases) <-- If this is not set up right...SV will not work :frowning: You can also use one of the custom versions below.

[Serious-Nuvotifer (Unofficial)](https://drive.google.com/file/d/0B2LjecPmLjo0ZmtDUm4xRm9KUEU/view?usp=sharing) 
Allows for the use of /nvreload, You no longer need to restart your server Server to make changes. 
[Serious-Nuvotifer API-7 (Unofficial)](https://drive.google.com/file/d/0B2LjecPmLjo0aTlMak5lWnJ2MVE/view?usp=sharing)
Same as the previous except works on API-7

Check us out on Ore! [To Ore](https://ore.spongepowered.org/curscascis/SeriousVote)

I often get asked the same questions with the same exact answers and I wanted to make a quick guide so we can save some time in the process. I don’t always have time to answer questions, and I don’t want anyone to feel like they’re being ignored. So here we go.   

#1 Question - _Can you help me? I’m not getting any rewards in game!_
----------------------
9/10 times this is happening because your NuVotifier is not setup correctly. My plugin does literally nothing if NuVotifier is not receiving votes. So please ensure NuVotifier is functioning otherwise you won’t get any action from SeriousVote (SV). 

### How do I find out if it's NuVotifier causing an issue?
This one is pretty easy, run a test vote to your server. First you will need to enable debug mode in NuVotifier.   
To enable debug mode: 
1. Go to your NuVotifier config folder. Edit the **Config.yml**. 
2. You are going to set the line with debug to `debug: true`. 
3. Save the file and restart your server. _(If you decided to use Serious-NuVotifier you can just do `/nvreload`)_    


Debug mode should now be enabled and you should see more output on your console. After you have that set up we will be firing a test vote to the server.

I like using https://mctools.org/votifier-tester just for its simplicity but I have also used other tools including local ones. Double and triple check to make sure the server information you put in the form fields is correct!!!! Send the vote and check your console for the output. A successful vote will show output like the output below. Upon restarting / reloading you will also see that NuVotifier loaded in debug mode.
![Example of server successfully receiving a vote](https://i.imgur.com/OBg9CPu.png)   
If you get output like the below, it means your public key is incorrect. The easiest way to fix this is to delete your **/rsa** folder and restart the server, it will generate new keys. Make sure to copy them exactly with no space before or after!   
![Example of server with broken rsa keys](https://i.imgur.com/97RtDUZ.png)

If you get no output at all, it means your network configuration is incorrect! This part is ***EXTREMELY*** Important. Here's a list of things you should look out for.
1. `host` Should either be 0.0.0.0 or 127.0.0.1 under almost all circumstances, unless you have a specific networking infrastructure and you know what you are doing, in which case it's unlikely you are looking for help here. 
2. `port` This one is usually the biggest problem because most people don't use the correct port.
    1. If you are hosting from home, you can use any port available on your computer, but you must _Port forward_ that port to your computer, otherwise you won't get any votes.
    2. If you are hosting from a MC host (BeastNode, Apex, Creeper, etc..) you will usually have to get which port you use from your server host. They will be able to provide you a information on what port to use. Often times they already provide a port for you to use which you can look up in your panel.
    3. For any server make sure your firewall is allowing those ports open. I will not be providing documentation on how to do that as there are thousands of possibilities.

3. `disable-v1-protocol` Should be false. Basically no one supports "v2" AFAIK, and there's no real need to use it. 
4. `forwarding` Unless you are setting up bungee or something, you can leave it as none. Those setups are more complex and you should be looking at the actual NuVotifier / Votifier documentation for that.    


#### For the other 1/10 times, the issue stems from a broken config. This is pretty straightforward:
1. Make sure you are saving your file with Unix Line Endings.
    1. If you use **Notepad++** you can change that from the menus. _Edit > EOL Conversion > Unix_
    2. If you use **VS Code** you can change it at the lower bottom. In the status area there will be some text that either says CRLF or LF. You can click there to make sure you are selecting LF.
2. Make sure you have a closing quote/parentheses/brace for every quote/parentheses/brace you place down. Make sure you have commas between items in a list. Etc..
3. It's a big config I know, I'll be working on improving it's ease of use. Look at the config guide for help on where to start.

If you require any help because you are not receiving rewards, these will be the first steps I will make you go through, and I will ask you for confirmation that you have done them. So start here and don't try to get a shortcut. [Discord](https://discord.gg/wH6r8Vm)


---
Has this project helped your community grow? Think about donating [:moneybag:Donate](https://paypal.me/iamabanana)
