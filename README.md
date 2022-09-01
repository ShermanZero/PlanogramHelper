# PlanogramHelper

Hey there someone who probably could hire me, let me tell you why you *should*:
- I made this in 6 hours because I was bored

![image](https://user-images.githubusercontent.com/16752746/187807356-41a5225e-439b-4fe6-a0f6-f7cb3a09d6bb.png)

### What does it do?
Makes life easier for everyone!

### Why does it do that?
Because I told it to

### When does it do that?
As soon as you use it!

### How does it do that?
Well I am so glad you asked.  Basically, you upload a fancy KinneyDrugs®-issued planogram, it does some fancy-shmancy behind the scenes work, and presto you've got yourself a handy-dandy list of every product contained in the planogram, searchable by SKU, UPC, or even words!  It'll spit out whatever it can find that matches, just select the items you're looking for, hit that oversized print button, and listen to the heartwarming sounds of machinery as your printer cries happy tears for you.

### I want a technical explanation
Well then you should stop wasting your time reading this and look through the repository.  But, if you *must* know, here's what happens after you upload a pdf:
- Apache's open source [PDFBox](https://pdfbox.apache.org/) goes into work parsing all the text from the file.
- Kieran's (me) open source PlanogramHelper (this) iterates through the text, looking to pattern match through two regular expressions:
  - `(?<POSITION>\d+)\s(?<SKU>\d+)\s(?=.*[a-zA-Z])(?<DESCRIPTION>(?:.*(?!\d+\W))*)\s(?<UPC>\d*)\s(?<FACINGS>\d)\s*(?<NEW>\bNEW\b){0,1}\n*`
  - `(?:Fixture)\s(?:(?!\d).)*(?<FIXTURE>(?:\w|[.])*)\s(?:Name)\s(?<NAME>.*)`
  
  The first pattern matches how products are displayed, and the second pattern matches how new sections are displayed.
- More machine language executes to make 1s and 0s make objects stored in HashMaps for easy and effecient O(1) lookups.
- Did I mention that was a threaded execution?  Of course it is!  We can't have the GUI hanging on the end-user after all.
- Did I also mention that thread also has custom piped input/output streams for the in-app toggleable developer console?
- Did I also also mention - oh it doesn't matter, there are many different advanced techniques used in this program to showcase my expertise.  Please look around and enjoy the thoroughly documented classes.

----

*And it all comes together cleanly, quickly, and presentably to allow you to do this in under 30 seconds:*

![image](https://user-images.githubusercontent.com/16752746/187807590-eaf35a17-a683-4670-b1d5-6581f985afc5.png)

![image](https://user-images.githubusercontent.com/16752746/187807629-7ea0d00c-7269-4f2c-bf85-99544ffcda28.png)

----
### Wow you're right, I should hire you!
Yes you should!  I have been programming since I was a young teenager.  Now in my mid-twenties, I have accumulated hundreds of thousands of hours across multiple languages including Java (obviously), C#, Python, PHP, MySQL, MongoDB, Git, Node.js, Javascript, TypeScript, HTML/CSS/SCSS, Svelte/SvelteKit, Unix/bash shell scripting, and so much more!

### How do I contact you?
Well, if you're from KinneyDrugs®, you probably already know how.  Otherwise, shoot me a DM over on my [Twitter](https://twitter.com/ShermanZero) (I guess people still use that, although not me really) or add me on Discord (much faster!) kieran#6425.


