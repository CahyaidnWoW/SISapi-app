<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\LivestockBreed;
use Illuminate\Http\Request;

class LivestockBreedController extends Controller
{
    public function index()
    {
        return response()->json(LivestockBreed::all());
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'breed_name' => 'required|string|max:255|unique:livestock_breeds,breed_name',
            'ideal_fcr_range' => 'nullable|string|max:50',
        ]);

        $breed = LivestockBreed::create($validated);

        return response()->json($breed, 201);
    }
}