# 8080-Emulator written in Java.
This is my Senoir Project @ Oregon Tech
Professor: Calvin Caldwell
Author: Joshua Hardy

Emulator functionally plays Space Invaders without sound.

This is one of my first large projects. I learned a lot.
I chose the 8080 emulator because I wanted to show the depth
of my knowledge gained from school, from the basics C to bit
banging to more OOP concepts.

How to run:
    1. Find dump 4 ROM files from an original 8080
    2. Merge the four files into one file in this order:
        1. invaders.h
        2. invader.g
        3. invaders.f
        4. invaders.e
	ex: cat invaders.h > space_invaders.rom
	    cat invaders.g >> space_invaders.rom
	    cat invaders.f >> space_invaders.rom
	    cat invaders.e >> space_invaders.rom
    3. Put space_invaders.rom into Emulator/src/roms/space_invaders.rom
    4. *If* wanted, insert sound files into Emulator/src/sounds/*.wav
    5. Compile with favorite Java IDE and play
