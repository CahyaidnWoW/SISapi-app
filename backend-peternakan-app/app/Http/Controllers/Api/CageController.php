<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Cage;
use Illuminate\Http\Request;

class CageController extends Controller
{
    public function index()
    {
        return response()->json(Cage::all());
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'capacity' => 'required|integer|min:1',
            'location' => 'nullable|string|max:255',
        ]);

        $cage = Cage::create([
            ...$validated,
            'current_population' => 0,
        ]);

        return response()->json($cage, 201);
    }

    public function show($id)
    {
        $cage = Cage::with('livestocks')->find($id);

        if (!$cage) {
            return response()->json(['message' => 'Kandang tidak ditemukan'], 404);
        }

        return response()->json($cage);
    }

    public function update(Request $request, $id)
    {
        $cage = Cage::findOrFail($id);

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'capacity' => 'required|integer|min:1',
            'location' => 'nullable|string|max:255',
        ]);

        $cage->update($validated);

        return response()->json($cage);
    }

    public function destroy($id)
    {
        $cage = Cage::findOrFail($id);

        if ($cage->livestocks()->count() > 0 || $cage->current_population > 0) {
            return response()->json([
                'message' => 'Kandang tidak dapat dihapus karena masih berisi sapi.'
            ], 400);
        }

        $cage->delete();

        return response()->json(['message' => 'Kandang berhasil dihapus']);
    }
}