![Logo](https://github.com/yuyakaido/images/blob/master/CardStackView/sample-logo.png)

# CardStackView

The fork of Yuya Kaido's ![CardStackView repository](https://github.com/yuyakaido/CardStackView) but optimized for my pet project.
It contains the optimization of view managing. Also, some functions such as the option "stackFrom" and automatic rewind were removed.
Animation of view scaling was also changed in purposes of optimization.

The observation: don't use CardView as the main component of your item. It causes lags.
Better not to use this repo, if you found it occasionally. Just use original Yuya Kaido's repo.

# Overview

![Overview](https://github.com/yuyakaido/images/blob/master/CardStackView/sample-overview.gif)

## Advanced usages

| Method | Description |
| :---- | :---- |
| CardStackView.smoothScrollToPosition(int position) | You can scroll any position with animation. |
| CardStackView.scrollToPosition(int position) | You can scroll any position without animation. |

# Callbacks

| Method | Description |
| :---- | :---- |
| CardStackListener.onCardDragging(Direction direction, float ratio) | This method is called while the card is dragging. |
| CardStackListener.onCardSwiped(Direction direction) | This method is called when the card is swiped. |
| CardStackListener.onCardRewound() | This method is called when the card is rewinded. |
| CardStackListener.onCardCanceled() | This method is called when the card is dragged less than threshold. |
| CardStackListener.onCardAppeared(View view, int position) | This method is called when the card appeared. |
| CardStackListener.onCardDisappeared(View view, int position) | This method is called when the card disappeared. |

# Installation

```groovy
dependencies {
    implementation 'com.github.CakeWalker1337:CardStackView:$latest_version_tag'
}
```

# License

```
Copyright 2018 yuyakaido

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
