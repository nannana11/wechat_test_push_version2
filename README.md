# how to create a wechat daily push?
## Steps
- go to this website and sigh in using wechat:https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login
- create a repository in github ,then put the appid and app secret into the repository secret
- copy all the files in my repository into your repository,change the content as you like
- in the wechat website,create a new content and copy contend.txt into the wechat website
- create workflow in the github

## A&Q
- A:how to change the time the workflow work？
- Q：open the "daily push.yml",the time which you can change is at the head of that file.

## Attention
due to the principle of the github workflow,the time you really get the daily push may be 5 to 30 minutes later than you expected.
