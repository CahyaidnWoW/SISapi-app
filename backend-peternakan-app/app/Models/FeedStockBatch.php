<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FeedStockBatch extends Model
{
    protected $guarded = ['id'];

    public function feedStock() {
        return $this->belongsTo(FeedStock::class, 'feed_id');
    }
}
