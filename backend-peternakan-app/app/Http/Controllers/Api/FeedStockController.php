<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\FeedStock;
use App\Models\FeedStockBatch;
use Illuminate\Http\Request;

class FeedStockController extends Controller
{
    public function index()
    {
        $feeds = FeedStock::withSum('batches as total_stock', 'quantity_remaining')->get();
        return response()->json($feeds);
    }

    public function addBatch(Request $request)
    {
        $validated = $request->validate([
            'feed_id' => 'required|exists:feed_stocks,id',
            'quantity_in' => 'required|numeric|min:0.01',
            'entry_date' => 'required|date',
        ]);

        $batch = FeedStockBatch::create([
            'feed_id' => $validated['feed_id'],
            'quantity_in' => $validated['quantity_in'],
            'quantity_remaining' => $validated['quantity_in'],
            'entry_date' => $validated['entry_date'],
        ]);

        return response()->json($batch, 201);
    }
}