# Minesweeper
This repo is a customizable recreation of the popular game minesweeper. It has everything the original minesweeper has, plus the ability to set a custom amount of rows, columns, and mines. Left clicking reveals a cell, while right clicking flags a cell.

### Setup
When creating an instance of the class Minesweeper, simply input the rows, cols, and mines you desire. Each game will be random, as long as you input a new Random() object as the third input in the constructor. Download the src file, and make sure to set up the run configuration with the project name, and "Main class" as "tester.Main". Click run with the correct configuration, and the game should pop up in a new window.

### Info
The development of this recreation was pretty straightforward. the biggest thing to note is the automatic flood fill feature, which when clicking on a tile with zero surrounding bombs, the game will automatically "flood" into the other possible empty cells, just how the actual minesweeper game works.

<img width="503" alt="Screenshot 2025-01-27 at 9 16 39 PM" src="https://github.com/user-attachments/assets/144f2108-364e-4d7f-8b94-4eb6db630101" />
