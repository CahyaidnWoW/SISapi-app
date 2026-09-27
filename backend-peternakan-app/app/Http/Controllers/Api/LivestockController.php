<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\LivestockService;
use Illuminate\Http\Request;

class LivestockController extends Controller
{
    protected LivestockService $livestockService;

    public function __construct(LivestockService $livestockService)
    {
        $this->livestockService = $livestockService;
    }

    public function index()
    {
        return response()->json($this->livestockService->getAll());
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'cage_id' => 'required|exists:cages,id',
            'breed_id' => 'required|exists:livestock_breeds,id',
            'gender' => 'required|in:male,female',
            'birth_date' => 'required|date',
            'entry_date' => 'required|date',
            'initial_weight' => 'required|numeric|min:0.01',
        ]);

        $livestock = $this->livestockService->create($validated);

        return response()->json($livestock, 201);
    }

    public function showByTag(string $tagNumber)
    {
        $result = $this->livestockService->findByTag($tagNumber);

        if (!$result) {
            return response()->json(['message' => 'Sapi tidak ditemukan'], 404);
        }

        return response()->json($result);
    }
}