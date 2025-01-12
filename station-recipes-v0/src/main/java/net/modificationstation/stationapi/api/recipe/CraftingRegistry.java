package net.modificationstation.stationapi.api.recipe;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.chars.Char2ReferenceMap;
import it.unimi.dsi.fastutil.chars.Char2ReferenceOpenHashMap;
import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.recipe.RecipeRegistry;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.tag.TagKey;
import net.modificationstation.stationapi.api.util.API;
import net.modificationstation.stationapi.impl.recipe.StationShapedRecipe;
import net.modificationstation.stationapi.impl.recipe.StationShapelessRecipe;

import java.util.Optional;

public class CraftingRegistry {

    @API
    public static void addShapedRecipe(ItemInstance output, Object... recipe) {
        StringBuilder ingredients = new StringBuilder();
        int currentElement = 0;
        int width = 0;
        int height = 0;
        if (recipe[currentElement] instanceof String[] grid) {
            currentElement++;
            for (int i = 0; i < grid.length; ++i) {
                String ingredientsLine = grid[i];
                ++height;
                width = ingredientsLine.length();
                ingredients.append(ingredientsLine);
            }
        } else while (recipe[currentElement] instanceof String ingredientsLine) {
            currentElement++;
            ++height;
            width = ingredientsLine.length();
            ingredients.append(ingredientsLine);
        }
        Char2ReferenceMap<Either<TagKey<ItemBase>, ItemInstance>> keyToIngredient = new Char2ReferenceOpenHashMap<>();
        for (; currentElement < recipe.length; currentElement += 2) {
            char c = (char) recipe[currentElement];
            Either<TagKey<ItemBase>, ItemInstance> ingredient = null;
            Object element = recipe[currentElement + 1];
            if (element instanceof TagKey<?> tag) {
                Optional<TagKey<ItemBase>> itemTagOpt = tag.tryCast(ItemRegistry.KEY);
                if (itemTagOpt.isPresent()) ingredient = Either.left(itemTagOpt.get());
            } else if (element instanceof BlockBase block) ingredient = Either.right(new ItemInstance(block, 1, -1));
            else if (element instanceof ItemInstance stack) ingredient = Either.right(stack);
            else if (element instanceof ItemBase item) ingredient = Either.right(new ItemInstance(item));
            keyToIngredient.put(c, ingredient);
        }
        //noinspection unchecked
        Either<TagKey<ItemBase>, ItemInstance>[] grid = new Either[width * height];
        for (int i = 0; i < width * height; ++i) {
            char c = ingredients.charAt(i);
            grid[i] = keyToIngredient.containsKey(c) ? keyToIngredient.get(c).mapRight(itemInstance1 -> itemInstance1.copy()) : null;
        }
        //noinspection unchecked
        RecipeRegistry.getInstance().getRecipes().add(new StationShapedRecipe(width, height, grid, output));
    }

    @API
    public static void addShapelessRecipe(ItemInstance output, Object... ingredients) {
        //noinspection unchecked
        Either<TagKey<ItemBase>, ItemInstance>[] checkedIngredients = new Either[ingredients.length];
        for (int i = 0, ingredientsLength = ingredients.length; i < ingredientsLength; i++) {
            Object ingredient = ingredients[i];
            if (ingredient instanceof ItemInstance stack) checkedIngredients[i] = Either.right(stack.copy());
            else if (ingredient instanceof ItemBase item) checkedIngredients[i] = Either.right(new ItemInstance(item));
            else if (ingredient instanceof BlockBase block) checkedIngredients[i] = Either.right(new ItemInstance(block));
            else if (ingredient instanceof TagKey<?> tag) {
                Optional<TagKey<ItemBase>> itemTagOpt = tag.tryCast(ItemRegistry.KEY);
                if (itemTagOpt.isPresent())
                    checkedIngredients[i] = Either.left(itemTagOpt.get());
            } else throw new RuntimeException("Invalid shapeless recipe ingredient of type " + ingredient.getClass().getName() + "!");
        }
        //noinspection unchecked
        RecipeRegistry.getInstance().getRecipes().add(new StationShapelessRecipe(output, checkedIngredients));
    }
}
