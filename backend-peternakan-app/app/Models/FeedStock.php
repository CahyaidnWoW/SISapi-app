<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FeedStock extends Model
{
    protected $guarded = ['id'];

    public function batches() {
        return $this->hasMany(FeedStockBatch::class, 'feed_id');
    }

    public function consumptions() {
        return $this->hasMany(FeedConsumption::class, 'feed_id');
    }
}
