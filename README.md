# Chat
## Idea
Chat program inspired by IRC.

Server does not store any client information - accounts are RSA key based and messages are not saved.

Client identity RSA keys are stored and password protected on the client.

Server key stored on the server.

At runtime communications are encrypted using a session DSA key (after a RSA key handshake).

All messages are signed by the RSA keys of the servers/clients.

## Instructions / basic usage
JDK 17 is required, 20 (21 ideally, but its EA) recommended for virtual threads

Download the project

run runserver.bat or runserver.sh to start the server

run runclient_1.bat or runclient_1.sh to run the first client

open the create account screen (bottom left)

enter a username

enter a password (and remember it)

hit create account

select your account in the dropdown/combobox

enter your password

hit login

verify that the server key matches the one printed in the server console window (or just hit Yes)

type messages in the center box

click the button or ctrl-enter to send

drag and drop small images onto the message box to send images

add a channel on the left panel (type a name and click the button, alphanumeric only, min 4 characters

click a channel on the sidebar to switch to it
right click any non-default channel to leave it
run a secondary or tertiary client using runclient2/3
repeat login process with new accounts

## Comments
I think there is an adaquate amount of comments

## External Libraries
junit 5

javaFX

log4J

gson

gradle (build tool)

## GUI and User Friendly
GUI scales quite well

panels are resizable

prompt texts for input

explainations for errors

## Data Persistence
Client keys, trusted servers, trusted users, username, channels are saved/persisted locally

Server key is persisted between runs

## Tests
tests for proper (de)serialization - core/io.github.marcus8448.chat.test.BinaryIOTests

fixed a signed-ness bug

## License
Licensed under the Apache License, version 2.0. See [LICENSE](./LICENSE) for more information.
