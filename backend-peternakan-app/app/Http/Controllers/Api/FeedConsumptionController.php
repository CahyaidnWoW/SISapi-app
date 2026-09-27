<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\FeedConsumptionService;
use Illuminate\Http\Request;
use Exception;

class FeedConsumptionController extends Controller
{
    protected FeedConsumptionService $feedConsumptionService;

    public function __construct(FeedConsumptionService $feedConsumptionService)
    {
        $this->feedConsumptionService = $feedConsumptionService;
    }

    public function store(Request $request, $id)
    {
        $request->merge(['cage_id' => $id]);

        $validated = $request->validate([
            'cage_id' => 'required|exists:cages,id',
            'feed_id' => 'required|exists:feed_stocks,id',
            'amount_used' => 'required|numeric|min:0.01',
            'date' => 'required|date',
        ]);

        try {
            $consumption = $this->feedConsumptionService->record($validated, $request->user()->id);

            return response()->json($consumption, 201);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 422);
        }
    }
}