# PlanogramHelper

Hey there someone who probably could hire me, let me tell you why you *should*:

![image](https://user-images.githubusercontent.com/16752746/188516429-61b3ae2d-2b4c-4f73-a2b4-0bd276f883be.png)

### What does it do?
Makes life easier for everyone!  (Finds any product in a planogram)

### Why does it do that?
Because I told it to!  (Because I told it to)

### When does it do that?
As soon as you use it!

### How does it do that?
So glad you asked!  Basically, you upload an epic KinneyDrugs®-issued planogram, it does some fancy-shmancy behind the scenes work, and presto you've got yourself a handy-dandy list of every product contained in the planogram, searchable by SKU, UPC, or even words!  It'll spit out whatever it can find that matches, just select the items you're looking for, hit that oversized print button, and listen to the heartwarming sounds of machinery as your printer cries happy tears for you.

### I want a technical explanation
Well then you should stop wasting your time reading this and look through the repository!  But, if you *must* know, here's what happens after you upload a pdf:
- Apache's open source [PDFBox](https://pdfbox.apache.org/) goes into work parsing all the text from the file.
- Kieran's (me) open source PlanogramHelper (this) iterates through the text, looking to pattern match through two regular expressions:
  - `(\d+)\s(\d+)\s(?=.*[a-zA-Z])((?:.*(?!\d+\W))*)\s(\d*)\s(\d)\s*(\bNEW\b){0,1}\n*`
  - `(?:Fixture)\s(?:(?!\d).)*((?:\w|[.])*)\s(?:Name)\s(.*)`
  
  The first pattern matches how products are displayed, and the second pattern matches how new sections are displayed.
- More machine language executes to make 1s and 0s turn into objects stored in HashMaps for easy and effecient O(1) lookups.
- Did I mention that was a threaded execution?  Of course it is!  We can't have the GUI hanging on the end-user after all.
- Did I also mention that thread also has custom piped input/output streams for the in-app toggleable developer console?
- Did I also also mention - oh it doesn't matter, there are many different advanced techniques used in this program to showcase my expertise.  Please look around and enjoy the thoroughly documented classes.  **I would start [here](src/main/java/pf/Processor.java) at the heart of the beast.**

### New to 0.3.0+
- Remote database fetching!
  - Now can pull (automatically on startup if desired, and manually) from a MongoDB database.
  - User access is read-only!
  - Developer console was updated to include "RESET" and "PUBLISH" buttons, but this will not work on machines except mine unless you can somehow figure out how to launch the .jar with the correct authorization/access tokens which allow the MongoDB driver to connect to my administrator account.

----

*And it all comes together cleanly, quickly, and presentably to allow you to do this in under 30 seconds:*

![image](https://user-images.githubusercontent.com/16752746/187807590-eaf35a17-a683-4670-b1d5-6581f985afc5.png)

![image](https://user-images.githubusercontent.com/16752746/187807629-7ea0d00c-7269-4f2c-bf85-99544ffcda28.png)

----

### Wow!
Wow indeed!  I have been programming since I was a young teenager.  I was the solo IT Lead for a school district in northern Maine when I was 19 years old, and I wrote my fair share of programs there too.  Now in my mid-twenties, I have accumulated hundreds of thousands of hours across multiple languages including Java (obviously), C#, Python, PHP, MySQL, MongoDB, Git, Node.js, Javascript, TypeScript, HTML/CSS/SCSS, Svelte/SvelteKit, Unix/bash shell scripting, and so much more!

### Have you heard of CTRL-F?
When I first started as a cashier to get my feet underneath myself after a rough move from Florida, I was frustrated by the lack of resources to find products I couldn't after my eyes glazed over a hundred times.  I specifically asked management if we had any way to reference planograms to find product, and was repeatedly told no.  Well, when I moved up to management and was given access to all managerial things, I started keeping downloaded planograms organized in a folder.  After a recent vitamin reset, there was a large batch of products left laying out, so I CTRL-F'd through the planogram to find if the products still existed in the aisle, if they were marked down, or if they entered limbo.  It sped up workflow significantly and I didn't have to waste my time or others' combing through hundreds of labels.

### Is this practical?
Absolutely!  Even in its current state it is very powerful, although there are many features I would develop if its purpose wasn't just to be an example.  And who knows, if you happen to fall in love with the idea and bring me onto your team, I would be more than happy to continue development and/or integrate with an existing service (SMS, I have my eyes on you).  I originally wrote PlanogramHelper using JDK18 but quickly realized after attempting to run it on systems at work we're only supporting the JRE8 environment (nice LTS choice!)  Side note - your systems distribution did prevent installation of Node.js, which was my original choice for this program, then it stopped Python too.  JDK18 wasn't allowed to run either despite downloading the binaries and modifying the user's PATH, but it does seem like a security risk that an executeable .jar can run as long as it's written to support JRE8.  Good news though, without the development binaries at least you can't compile .java files locally!  Also, our intranet has no redirects.  A simple browser query with / accesses the entire folder structure and you are free to browse - a .htaccess update might be warranted there.  I have a small background in cybersecurity.

### What additions would you add?
For starters, I would absolutely be running a database server on the backend that the frontend can pull from.  Since product information seems to be pretty immutable, a SQL database would make sense, but I do love a NoSQL database like Mongo.  Either way, a database that stores all this information fetch-able through as-needed frontend update prompts would save lots of resource/processing power and ensure all distributions are synchronized.  It wouldn't surprise me if this database already exists, even if it's just an uncomfortably large excel spreadsheet, but imagine the possibilities of a hosted database.  For enders, that's about it.  There are bugs that need to be fixed (like being allowed to open multiple settings screens), but the largest and most significant change would be a program that doesn't have to use all 2kb of memory on our old computers to parse through a pdf.

### What people are saying
- > "Truly an incredible masterpiece :tada:" ~Kieran Skvortsov (me)
- > "You need to be paid more" ~Dylan Pritchard (current coworker)
- > "Megan agrees you need to be paid more" ~Dylan Pritchard (still a current coworker, referencing his wife Megan)
- > "That's honestly the best thing omg!" ~Sici Khars (former managerial coworker)

### How do I contact you?
Well, if you're from KinneyDrugs®, you probably already know how since I brought you here.  Otherwise, add me on Discord kieran#6425, or reach me at my professional email found in my GitHub profile.

Thank you for your time, I really appreciate it, and I look forward to hearing from you!
