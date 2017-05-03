{-# LANGUAGE OverloadedStrings #-}

import Control.Monad (forM_)
import Text.Blaze.Html5 as H hiding (main)
import Text.Blaze.Html5.Attributes as A
import Text.Blaze.Html.Renderer.String as S

numbers :: Int -> Html

numbers n = docTypeHtml $ do
  H.head $ do
    H.title "Natural numbers"
  body $ do
    p "A list of natural numbers:"
    ul $ forM_ [1 .. n] (li . toHtml)

main =
  do
    putStrLn $ S.renderHtml (numbers 10)
