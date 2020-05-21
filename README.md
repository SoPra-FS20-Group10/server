#README Sopra FS20 Group 10: Scrabbar.io

Scrabbar.io is an online version of the famous board game Scrabble. Our game is
playable by 2-4 players and follows the original game's rules.

## Introduction

Hello there, we are a small group of university students who programmed this game
for a software engineering lab called Sopra. This is the back-end of our project.

Our main goal for this project was to learn how to efficiently work in a group of
software developers. This includes communication, coordination and of course having
fun programming our game ;).

It was also very important to us that we have a game we can be proud of. This includes
having a frontend that is visually appealing, and a backend that works even when there
are faulty requests.

## Technologies used

For this project we used java to implement our server. In particular, we used JPA and Spring
for our web server and the wordnik API for our word validation system.

Additionally, we used Slack and Jira for communication and coordination of our team
and workload. Both products have been used hand-in-hand with our frontend team.

## High-level components

### REST

#### Controllers (src/main/java/ch/uzh/ifi/seal/soprafs20/controller)

Using controllers we build a REST interface that is the main component used for communication with the front-end.
The different requests are handled in dedicated controllers that were split by functionality.

#### DTO's and DTOMapper (src/main/java/ch/uzh/ifi/seal/soprafs20/rest/(dto || mapper))

to make the entities compatible with the front-end and the specific requests, we introduced DTO's to our system,
so we can have uniform Objects for the communication between front- and back-end. The DTO Mapper converts the entities
to its external/API representation.

### Entities and repositories (src/main/java/ch/uzh/ifi/seal/soprafs20/(entity || repository))

We split the basic elements of our game and abstracted them into seperate entities that are stored in respective
repositories in the database. This allows us to handle clean collaboration of elements and persistent storage
of information.

### Service (src/main/java/ch/uzh/ifi/seal/soprafs20/service)

The service classes of our application are the heart of our system and handle all the logical details. For every complex entity
(f.i. chats or game) we have a service class that handels all its actions.

### Tests (src/test)

We have thorough testing with a test coverage of > 75%. Next to basic testing methods, we also use Mockito to "simulate"
our Spring instances, to enable high quality tests.

## Launch & Deployment

### "gradlew build (--continuous)"

This command builds your project using gradle. The "--continuous" option can be used to automatically build your
project after modification

### "gradlew bootrun"

This command runs your application locally on your system on [http://localhost:8080](http://localhost:8080)

## Roadmap

Some additional features which would be nice to have would be:

* Spectator Mode
* Password Recovery System
* Registering with Email

## Authors and acknowledgement

Tim Brunner
Jan Schnyder
Pascal Marty
Patrick Looser


## License

Copyright 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

see the LICENSE.md file for details.