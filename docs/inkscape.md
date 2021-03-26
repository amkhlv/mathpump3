# Hints on using Inkscape

For formulas, I recommend the calligraphic tool (keyboard shortcut `c`).

## Setting up color switching

The procedure for changing drawing color is __very confusing__ 
In my opinion, this is one great drawback of Inkscape.
But it can be remedied by proper training. 
I will try now to give clear instructions on how to set up color switching. 
(After it is set up correctly, it becomes very easy and handy.)

### Choosing style: step 1

First of all, we need to set up what they call "style". We need to keep in mind:

1. Each drawing tool has its own "style". 

1. The style for each tool has two "regimes": `Last used style` and `This tool's own style`.
   The one which is called `This tool's own style` is not useful for us. We need to __choose `Last used style`__.

So, the first thing is to select a tool.

The procedure is more or less the same for all drawing tools, I will concentrate on the calligraphic pen.

Pressing `c` we select __calligraphic pen__ . Then, somewhere (usually in the upper right corner) appears `Fill/Stroke` selector:

![Fill-stroke selector](images/fill-and-stroke.png?raw=true)

Double click on that `Fill/Stroke` selector and select __Last used style__

Then __draw something__ with the calligraphic pen. The colors may be wrong, that's OK for now.

### Choosing style: step 2

In the case of __calligraphic pen__, it is wise to __disable `Stroke` and only use `Fill`__
Then, the color will be determined by the __Fill color__ (and the Stroke color, obviously, will be irrelevant).

So, how do we do it? 

Here comes probably the most confusing part. We must __exit the calligraphic pen and return to the "select" regime__.
(This can be done either by pressing either `Esc` or  `F3`.)
Then, we select that _something_ which we have just drawn in Step 1. 
Having it selected, in __Menu Bar__, go to `Object` -> `Fill and Stroke...` . The fill and stroke menu will appear.
Go to the `Stroke paint` tab of the selector, and disable stroke. (It should be saying "No paint")
Then go to the `Fill` tab and choose `Flat color` (the solid square). There you also choose `Alpha` (opacity). 
I usually just choose max opacity, 255.

### Now it is easy to switch colors

We are essentially done! Go back to the __calligraphic pen__ regime, by pressing `c`. 
Now you can click on a color in the color bar, and the pen turns that color!

(This setup is rather stable, Inkscape will remember it. You will likely never need again to go through those steps.)

## Setting SVG size

![Setting image size](images/inkscape_set-image-size.png?raw=true)

## Zooms

I find it __crucial to use zoom-in of Inkscape__ ; when drawing a formula I typically set zoom
to about 300% . You can zoom to a region by marking rectangle with pressed `Shift` and
lower button of e-pen, together. Just pressing lower button of e-pen moves the whiteboard.
(Of course, your audience does not see that you zoom. This is one advantage over desktop sharing.)
On the other hand, when zoomed-out, the drawn lines become thicker. If you need to highlight
a formula, just zoom out and then draw a red circle around it. 

    1	zoom 1:1
    2	zoom 1:2
    3	zoom to selection
    4	zoom to drawing
    5	zoom to page

Zoom history:

    `	(back quote) previous zoom
    Shift+`	next zoom

(it is stacked!)


## Complete list of keyboard shortcuts

Can be found [here](http://www-mdp.eng.cam.ac.uk/web/CD/deskapps/inkscape/keyshortcuts.html)


