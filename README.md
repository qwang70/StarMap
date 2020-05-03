# StarMap

## *Qiwen Wang*

**Star Map** displays a list of maps, each of which show user-defined markers with a title, description, and location. The user can also create a new map, delete and search maps. 

Time spent: **12** hours spent in total

## Functionality 

The following **required** functionality is completed:

* [X] The list of map titles is displayed.
* [X] After tapping on a map title, the associated markers in the map are shown.
* [X] The user is able to create a new map.

The following **extensions** are implemented:

* [X] Show the number of places in each map along with the title.
* [X] Use a purple marker to match the map theme
* [X] When a map marker is created, the pin is animated.
* [X] User can change the map type (e.g. normal vs terrain)
* [X] Add a search menu option and implement the Filterable interface in the adapter.
* [X] Store maps in a Room database
* [X] Allow map deletion

## Video Walkthrough

Here's a walkthrough of implemented user stories:

### Control flow
- Search maps
- Map insertion
- Map deletion
<img src='https://i.imgur.com/OOwrUNr.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />

### Display map
- Create a new map
- Add places
- Terrain view
- Save map
<img src='https://i.imgur.com/zmJCS7e.gif' title='Video Walkthrough2' width='' alt='Video Walkthrough2' />

## Notes
### Challenge using Room
When defining the entity with Room, I used a type converter to convert Places to string so that it can be stored in Room.
The downside is that Room can't easily do a query search on Places

### Challenge implementing terrain view
When I tried to add a switch for showing the terrain view on the top action bar in the menu, the menu can't find the Id of the switch. I used a checkbox in the dropdown box instead.


## License

    Copyright 2020 Qiwen Wang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
